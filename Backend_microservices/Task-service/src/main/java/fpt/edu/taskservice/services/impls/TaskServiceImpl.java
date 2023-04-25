package fpt.edu.taskservice.services.impls;

import fpt.edu.taskservice.dtos.requestDtos.EditStatusRequest;
import fpt.edu.taskservice.dtos.requestDtos.EditTaskRequest;
import fpt.edu.taskservice.dtos.requestDtos.NewTaskRequest;
import fpt.edu.taskservice.dtos.responseDtos.TaskResponse;
import fpt.edu.taskservice.entities.Task;
import fpt.edu.taskservice.enums.TaskStatus;
import fpt.edu.taskservice.pagination.Pagination;
import fpt.edu.taskservice.repositories.ProjectRepository;
import fpt.edu.taskservice.repositories.TaskRepository;
import fpt.edu.taskservice.services.TaskService;
import fpt.edu.taskservice.services.validations.ValidationHandler;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * @author Truong Duc Duong
 */

@Service
@Transactional(rollbackFor = Exception.class)
@Log4j2
public class TaskServiceImpl extends BaseService<Task> implements TaskService {
    private static final String DEFAULT_REQUEST_DATE_PATTERN = "dd-MM-yyyy";

    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    ProjectRepository projectRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private ValidationHandler validationHandler;
    @Autowired
    private Environment env;
    @Autowired
    private HandlerMapping handlerMapping;
    @Autowired
    private WebSocketHandler webSocketHandler;

    @Override
    public Mono<TaskResponse> save(NewTaskRequest newTaskRequest, ServerWebExchange exchange) {
        validationHandler.validate(newTaskRequest);

        return projectRepository.findById(newTaskRequest.getProjectId())
                .switchIfEmpty(Mono.error(new NotFoundException("Project id '" + newTaskRequest.getProjectId() + "' not found")))
                .zipWith(Mono.just(newTaskRequest), ((project, request) -> {
                    Task preparedTask = modelMapper.map(request, Task.class);
                    preparedTask.setProject(project);
                    super.setCreatedBy(preparedTask, exchange);
                    return preparedTask;
                }))
                .flatMap(preparedTask -> setDueAt(preparedTask, newTaskRequest))
                .flatMap(preparedTask -> taskRepository.save(preparedTask))
                .flatMap(super::buildTaskResponse)
                .doOnSuccess(taskResponse -> {
                    log.info("Task with id '{}' saved successfully", taskResponse.getId());

                    @SuppressWarnings("unchecked")
                    Map<String, WebSocketHandler> urlMapping = (Map<String, WebSocketHandler>) ((SimpleUrlHandlerMapping) handlerMapping).getUrlMap();
                    urlMapping.put("/comments/" + taskResponse.getId(), webSocketHandler);
                })
                .doOnError(throwable -> log.error(throwable.getMessage()));
    }

    @Override
    public Mono<TaskResponse> update(String id, EditTaskRequest editTaskRequest, ServerWebExchange exchange) {
        validationHandler.validate(editTaskRequest);

        return taskRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Task not found")))
                .flatMap(super::buildTask)
                .zipWith(Mono.just(editTaskRequest), ((currentTask, request) -> {
                    Task preparedTask = modelMapper.map(request, Task.class);

                    preparedTask.setProject(currentTask.getProject());
                    preparedTask.setCreatedAt(currentTask.getCreatedAt());
                    preparedTask.setCreatedBy(currentTask.getCreatedBy());
                    super.setUpdatedBy(preparedTask, exchange);
                    preparedTask.setId(id);
                    return preparedTask;
                }))
                .flatMap(preparedTask -> this.setDueAt(preparedTask, editTaskRequest))
                .map(preparedTask -> {
                    if (preparedTask.getDueAt().compareTo(new Date()) >= 0 && preparedTask.getStatus() == TaskStatus.OVERDUE.getCode()) {
                        preparedTask.setStatus(TaskStatus.IN_PROGRESS.getCode());
                    } else if (preparedTask.getDueAt().compareTo(new Date()) < 0 && preparedTask.getStatus() != TaskStatus.COMPLETED.getCode()) {
                        preparedTask.setStatus(TaskStatus.OVERDUE.getCode());
                    }
                    return preparedTask;
                })
                .flatMap(preparedTask -> taskRepository.save(preparedTask))
                .flatMap(super::buildTaskResponse)
                .doOnSuccess(taskResponse -> log.info("Task with id '{}' edited successfully", taskResponse.getId()))
                .doOnError(throwable -> log.error(throwable.getMessage()));
    }

    @Override
    public Mono<TaskResponse> updateStatus(String id, EditStatusRequest editStatusRequest, ServerWebExchange exchange) {
        String statusMessage = TaskStatus.getMessage(editStatusRequest.getStatus());
        return taskRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Task not found")))
                .flatMap(super::buildTask)
                .map(task -> {
                    task.setStatus(editStatusRequest.getStatus());
                    super.setUpdatedBy(task, exchange);
                    return task;
                })
                .flatMap(task -> taskRepository.save(task))
                .flatMap(super::buildTaskResponse)
                .doOnSuccess(taskResponse -> log.info("Task with id '{}' has new status: '{}'", taskResponse.getId(), statusMessage))
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
                .switchIfEmpty(Mono.error(new NotFoundException("Task not found")))
                .flatMap(super::buildTaskResponse)
                .doOnError(throwable -> log.error(throwable.getMessage()));
    }

    @Override
    public Mono<Void> delete(String id) {
        return taskRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException( "Task not found")))
                .flatMap(super::deleteAttachmentsByTask)
                .flatMap(super::deleteCheckItemsByTask)
                .flatMap(super::deleteCommentsByTask)
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
                .switchIfEmpty(Mono.error(new NotFoundException("Project id '" + projectId + "' not found")))
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
                .switchIfEmpty(Mono.error(new NotFoundException("Project id '" + projectId + "' not found")))
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
                .switchIfEmpty(Mono.error(new NotFoundException("Project id '" + projectId + "' not found or not authorized")))
                .flatMap(project -> taskRepository.findAllByProject(project))
                .filter(task -> task.getStatus() == status);

        return this.buildTaskResponseFlux(authorizedTaskFlux, pagination);
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

    @Scheduled(cron = "0 1 0 * * ?", zone = "Asia/Ho_Chi_Minh")
    private void setOverdueTasks() {
        taskRepository.findAll()
                .filter(task -> (task.getStatus() == TaskStatus.INITIATED.getCode() || task.getStatus() == TaskStatus.IN_PROGRESS.getCode()) && task.getDueAt().compareTo(new Date()) < 0)
                .map(task -> {
                    task.setStatus(TaskStatus.OVERDUE.getCode());
                    return task;
                })
                .flatMap(super::buildTask)
                .collectList()
                .subscribe(
                        overdueTasks -> taskRepository.saveAll(overdueTasks).subscribe(),
                        error -> log.error(error.getMessage()),
                        () -> log.info("Overdue tasks set")
                );
    }
}
