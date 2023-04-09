package fpt.edu.taskservice.services.impls;

import fpt.edu.taskservice.dtos.exchangeDtos.ExchangeAttachment;
import fpt.edu.taskservice.dtos.requestDtos.EditTaskRequest;
import fpt.edu.taskservice.dtos.requestDtos.NewTaskRequest;
import fpt.edu.taskservice.dtos.responseDtos.TaskResponse;
import fpt.edu.taskservice.entities.Attachment;
import fpt.edu.taskservice.entities.Task;
import fpt.edu.taskservice.pagination.Pagination;
import fpt.edu.taskservice.repositories.AttachmentRepository;
import fpt.edu.taskservice.repositories.ProjectRepository;
import fpt.edu.taskservice.repositories.TaskRepository;
import fpt.edu.taskservice.services.TaskService;
import fpt.edu.taskservice.services.validations.ValidationHandler;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author Truong Duc Duong
 */

@Service
@Transactional(rollbackFor = Exception.class)
@Log4j2
public class TaskServiceImpl extends BaseService<Task> implements TaskService {

    @Value("${folder.uploads}")
    private String UPLOADS_TMP;
    @Value("${folder.uploads.attachments}")
    private String ATTACHMENT_FOLDER;

    @Value("${rabbitmq.exchange}")
    private String EXCHANGE_NAME;

    @Value("${rabbitmq.routing-key.attachment-processing}")
    private String ATTACHMENT_PROCESSING_ROUTING_KEY;

    private static final String DEFAULT_REQUEST_DATE_PATTERN = "dd-MM-yyyy";

    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private AttachmentRepository attachmentRepository;
    @Autowired
    ProjectRepository projectRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private ValidationHandler validationHandler;
    @Autowired
    private Environment env;

    @Override
    public Mono<TaskResponse> save(NewTaskRequest newTaskRequest, ServerWebExchange exchange) {
        validationHandler.validate(newTaskRequest);
        return projectRepository.findById(newTaskRequest.getProjectId())
                .switchIfEmpty(Mono.error(new NotFoundException( "Project not found")))
                .zipWith(Mono.just(newTaskRequest), ((project, request) -> {
                    Task preparedTask = modelMapper.map(request, Task.class);
                    preparedTask.setProject(project);
                    super.setCreatedBy(preparedTask, exchange);
                    return preparedTask;
                }))
                .flatMap(preparedTask -> setDueAt(preparedTask, newTaskRequest))
                .flatMap(preparedTask -> taskRepository.save(preparedTask))
                .flatMap(task -> this.saveNewAttachments(newTaskRequest, task, exchange))
                .flatMap(super::buildTaskResponse)
                .doOnSuccess(taskResponse -> log.info("Task with id '{}' saved successfully", taskResponse.getId()))
                .doOnError(throwable -> log.error(throwable.getMessage()));
    }

    @Override
    public Mono<TaskResponse> update(String id, EditTaskRequest editTaskRequest, ServerWebExchange exchange) {
        return taskRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException( "Project not found")))
                .zipWith(Mono.just(editTaskRequest), ((currentTask, request) -> {
                    Task preparedTask = modelMapper.map(request, Task.class);
                    preparedTask.setProject(currentTask.getProject());
                    super.setUpdatedBy(preparedTask, exchange);
                    preparedTask.setId(id);
                    return preparedTask;
                }))
                .flatMap(preparedTask -> setDueAt(preparedTask, editTaskRequest))
                .flatMap(preparedTask -> taskRepository.save(preparedTask))
                .flatMap(task -> this.saveNewAttachments(editTaskRequest, task, exchange))
                .flatMap(super::buildTaskResponse)
                .doOnSuccess(taskResponse -> log.info("Task with id '{}' saved successfully", taskResponse.getId()))
                .doOnError(throwable -> log.error(throwable.getMessage()));
    }

    @Override
    public Flux<TaskResponse> getAll(Pagination pagination) {
        return super.paginate(taskRepository.findAll(), pagination)
                .flatMapSequential(super::buildTaskResponse)
                .delayElements(Duration.ofMillis(100))
                .doOnError(throwable -> log.error(throwable.getMessage()));
    }

    @Override
    public Mono<TaskResponse> get(String id) {
        return taskRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException( "Task not found")))
                .flatMap(super::buildTaskResponse)
                .doOnError(throwable -> log.error(throwable.getMessage()));
    }

    @Override
    public Mono<Void> delete(String id) {
        return taskRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException( "Task not found")))
                .flatMap(super::deleteAttachmentsByTask)
                .flatMap(task -> taskRepository.delete(task))
                .doOnSuccess(voidValue -> log.info("Task with id '{}' deleted", id))
                .doOnError(throwable -> log.error(throwable.getMessage()));
    }

    @Override
    public Flux<TaskResponse> getTasks(int taskStatusCode, Pagination pagination) {
        Flux<Task> taskFlux = taskRepository.findAll()
                .filter(task -> task.getStatus() == taskStatusCode);

        return super.paginate(taskFlux, pagination)
                .flatMapSequential(super::buildTaskResponse)
                .delayElements(Duration.ofMillis(100))
                .doOnError(throwable -> log.error(throwable.getMessage()));
    }

    @Override
    public Flux<TaskResponse> getTasksByProject(String projectId, int status, Pagination pagination) {
        Flux<Task> taskFlux = projectRepository.findById(projectId)
                .flatMapMany(project -> taskRepository.findAllByProject(project))
                .filter(task -> task.getStatus() == status);

        return this.buildTaskResponseFlux(taskFlux, pagination);
    }

    @Override
    public Flux<TaskResponse> getAssignedTasks(int status, Pagination pagination, ServerWebExchange exchange) {
        int authId = super.getAuthId(exchange);

        Flux<Task> assignedTaskFlux = taskRepository.findAll()
                .filter(task -> task.getUserIds() != null && task.getUserIds().contains(authId) && task.getStatus() == status);

        return this.buildTaskResponseFlux(assignedTaskFlux, pagination);
    }

    @Override
    public Flux<TaskResponse> getAssignedTasksByProject(String projectId, int status, Pagination pagination, ServerWebExchange exchange) {
        int authId = super.getAuthId(exchange);

        Flux<Task> assignedTaskFlux = projectRepository.findById(projectId)
                .flatMapMany(project -> taskRepository.findAllByProject(project))
                .filter(task -> task.getUserIds() != null && task.getUserIds().contains(authId) && task.getStatus() == status);

        return this.buildTaskResponseFlux(assignedTaskFlux, pagination);
    }

    @Override
    public Flux<TaskResponse> getAuthorizedTasks(int status, Pagination pagination, ServerWebExchange exchange) {
        Flux<Task> authorizedTaskFlux = super.getAuthorizedProjects(exchange)
                .flatMap(project -> taskRepository.findAllByProject(project))
                .filter(task -> task.getStatus() == status);

        return this.buildTaskResponseFlux(authorizedTaskFlux, pagination);
    }

    @Override
    public Flux<TaskResponse> getAuthorizedTasksByProjectId(String projectId, int status, Pagination pagination, ServerWebExchange exchange) {
        Flux<Task> authorizedTaskFlux = super.getAuthorizedProjects(exchange)
                .filter(project -> projectId.equals(project.getId()))
                .flatMap(project -> taskRepository.findAllByProject(project))
                .filter(task -> task.getStatus() == status);

        return this.buildTaskResponseFlux(authorizedTaskFlux, pagination);
    }

    @SuppressWarnings("unchecked")
    private Mono<Task> saveNewAttachments(Object taskRequest, Task task, ServerWebExchange exchange) {
        List<FilePart> newAttachments;
        try {
            Method method = taskRequest.getClass().getMethod("getAttachments");
            newAttachments = (List<FilePart>) method.invoke(taskRequest);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        return Mono.justOrEmpty(newAttachments)
                .filter(Objects::nonNull)
                .flatMapMany(Flux::fromIterable)
                .flatMap(filePart -> this.processAttachmentFile(filePart, task))
                .map(filePart -> new Attachment(filePart.filename(), task))
                .map(attachment -> {
                    this.setCreatedBy(attachment, exchange);
                    return attachment;
                })
                .collectList()
                .flatMap(attachments -> attachmentRepository.saveAll(attachments).collectList())
                .map(attachments -> {
                    List<String> filenames = attachments.stream()
                            .map(Attachment::getName)
                            .toList();
                    log.info("Attachments {} have been attached to task {}", filenames, task.getName());

                    return task;
                })
                .doOnError(throwable -> log.error(throwable.getMessage()))
                .defaultIfEmpty(task);
    }

    private Mono<FilePart> processAttachmentFile(FilePart filePart, Task task) {
        String attachmentFolderDirectory = System.getProperty("user.dir") + File.separator + UPLOADS_TMP + File.separator + ATTACHMENT_FOLDER;

//        create folder in case of non-existing
        String folderDirectory = attachmentFolderDirectory + File.separator + task.getId();
        Path folderPath = Paths.get(folderDirectory);
        File folder = folderPath.toFile();
        if (!folder.exists()) {
            boolean newFolderResult = folder.mkdirs();
            if (newFolderResult) {
                log.info("Create new folder {}" , folderDirectory);
            } else {
                log.error("Failed to create new folder: {}", folderDirectory);
            }
        }

        return filePart.transferTo(folderPath.resolve(filePart.filename()))
                .thenReturn(filePart)
                .doOnSuccess(fp -> log.info("File {} has been stored in folder {}", filePart.filename(), folderDirectory))
                .map(fp ->  {
                    ExchangeAttachment exchangeAttachment = new ExchangeAttachment(task.getId(), filePart.filename());

//                    send message to ask File-service to retrieve uploaded attachments
                    rabbitTemplate.convertAndSend(EXCHANGE_NAME, ATTACHMENT_PROCESSING_ROUTING_KEY, exchangeAttachment);
                    log.info("Message has been published via routing key '{}'", ATTACHMENT_PROCESSING_ROUTING_KEY);

                    return filePart;
                });
    }

    private Mono<Task> setDueAt(Task task, Object taskRequest){
        try {
            Method method = taskRequest.getClass().getMethod("getDueDate");
            String requestedDueAtStr = (String) method.invoke(taskRequest);

            Date requestedDueAt = null;
            if (StringUtils.hasText(requestedDueAtStr)) {
                requestedDueAt = super.parseDate(requestedDueAtStr, env.getProperty("date.pattern.request", DEFAULT_REQUEST_DATE_PATTERN));
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.WEEK_OF_YEAR, 2);
            Date autoGeneratedDueAt = calendar.getTime();

            return Mono.justOrEmpty(requestedDueAt)
                    .defaultIfEmpty(autoGeneratedDueAt)
                    .map(date -> {
                        task.setDueAt(date);
                        return task;
                    });
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private Flux<TaskResponse> buildTaskResponseFlux(Flux<Task> taskFlux, Pagination pagination) {
        return super.paginate(taskFlux, pagination)
                .flatMapSequential(super::buildTaskResponse)
                .delayElements(Duration.ofMillis(100))
                .doOnError(throwable -> log.error(throwable.getMessage()));
    }
}
