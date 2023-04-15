package fpt.edu.taskservice.services.impls;

import fpt.edu.taskservice.dtos.exchangeDtos.ExchangeAttachment;
import fpt.edu.taskservice.dtos.responseDtos.AttachmentResponse;
import fpt.edu.taskservice.dtos.responseDtos.TaskResponse;
import fpt.edu.taskservice.entities.Attachment;
import fpt.edu.taskservice.entities.Project;
import fpt.edu.taskservice.entities.Task;
import fpt.edu.taskservice.exceptions.UnauthorizedException;
import fpt.edu.taskservice.pagination.Pagination;
import fpt.edu.taskservice.repositories.AttachmentRepository;
import fpt.edu.taskservice.repositories.ProjectRepository;
import fpt.edu.taskservice.repositories.TaskRepository;
import jakarta.ws.rs.BadRequestException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * @author Truong Duc Duong
 */

@Log4j2
public class BaseService<T> {

    @Value("${http.request.auth.id}")
    private String AUTH_ID;

    @Value("${folder.uploads}")
    private String UPLOADS_TMP;
    @Value("${folder.uploads.attachments}")
    private String ATTACHMENT_FOLDER;

    @Autowired
    private AttachmentRepository attachmentRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private TaskRepository taskRepository;

    protected void setCreatedBy(Object object, ServerWebExchange exchange) {
        String authUserIdString = exchange.getRequest().getHeaders().getFirst(AUTH_ID);
        log.info("Auth_id: {}", authUserIdString);
        if (StringUtils.hasText(authUserIdString)) {
            int authUserId = Integer.parseInt(authUserIdString);
            try {
                Method method = object.getClass().getMethod("setCreatedBy", int.class);
                method.invoke(object, authUserId);

            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected void setUpdatedBy(Object object, ServerWebExchange exchange) {
        String authUserIdString = exchange.getRequest().getHeaders().getFirst(AUTH_ID);
        if (StringUtils.hasText(authUserIdString)) {
            int authUserId = Integer.parseInt(authUserIdString);
            try {
                Method method = object.getClass().getMethod("setUpdatedBy", int.class);
                method.invoke(object, authUserId);

            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected int getAuthId(ServerWebExchange exchange) {
        String authUserIdString = exchange.getRequest().getHeaders().getFirst(AUTH_ID);
        if (!StringUtils.hasText(authUserIdString)) {
            throw new UnauthorizedException("Unauthorized");
        }
        return Integer.parseInt(authUserIdString);
    }

    protected Date parseDate(String dateString, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        try {
            return sdf.parse(dateString);
        } catch (ParseException e) {
            throw new BadRequestException("Invalid date format: " + dateString);
        }
    }

    protected String getFieldValue(Object object, String fieldName) {
        if (!StringUtils.hasText(fieldName) || object == null) {
            return "";
        }
        Class<?> clazz = object.getClass();
        try {
            Field field = this.getField(clazz, fieldName);
            if (field != null) {
                field.setAccessible(true);
                return String.valueOf(field.get(object));
            } else {
                return "";
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return "";
        }
    }

    private Field getField(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass!= null) {
                return getField(clazz.getSuperclass(), fieldName);
            } else {
                e.printStackTrace();
                return null;
            }
        }
    }

    protected Flux<T> paginate(Flux<T> objectFlux, Pagination pagination) {
        if (pagination == null) {
            return objectFlux;
        } else {
            Flux<T> sortedObjectFlux;
            if (pagination.getDirection().equals(Pagination.ASC)) {
                sortedObjectFlux = objectFlux.sort(Comparator.comparing(object -> this.getFieldValue(object, pagination.getSortBy())));
            } else if (pagination.getDirection().equals(Pagination.DESC)) {
                sortedObjectFlux = objectFlux.sort(Comparator.comparing(object -> this.getFieldValue(object, pagination.getSortBy())).reversed());
            } else {
                throw new BadRequestException("Sort direction must be either '" + Pagination.ASC + "' or '" + Pagination.DESC + "'");
            }

            if (Pagination.isPaginationValid(pagination)) {
                return sortedObjectFlux
                        .skip((long) (pagination.getPageNumber() - 1) * pagination.getPageSize())
                        .take(pagination.getPageSize());
            } else {
                return sortedObjectFlux;
            }
        }
    }

    protected Mono<Project> buildProject(Project project) {
        Mono<List<Task>> taskListMono = taskRepository.findAllByProject(project)
                .collectList();

        return Mono.just(project)
                .zipWith(taskListMono, (preparedProject, tasks) -> {
                    preparedProject.setTasks(tasks);
                    return preparedProject;
                })
                .doOnError(throwable -> log.error(throwable.getMessage()));
    }

    protected Mono<Task> buildTask(Task task) {
        Mono<List<Attachment>> attachmentListMono = attachmentRepository.findAllByTask(task)
                .collectList();

        Mono<List<Project>> projectListMono = projectRepository.findAll()
                .flatMap(this::buildProject)
                .collectList();

        return Mono.just(task)
                .zipWith(attachmentListMono, (preparedTask, attachments) -> {
                    preparedTask.setAttachments(attachments);
                    return preparedTask;
                })
                .zipWith(projectListMono, (preparedTask, projects) -> {
                    Project parentProject = projects.stream()
                            .filter(project -> project.getTasks().stream().anyMatch(childTask -> preparedTask.getId().equals(childTask.getId())))
                            .findFirst()
                            .orElse(null);
                    preparedTask.setProject(parentProject);
                    return preparedTask;
                })
                .doOnError(throwable -> log.error(throwable.getMessage()));
    }

    protected Mono<TaskResponse> buildTaskResponse(Task task) {
        Mono<List<AttachmentResponse>> attachmentResponsesMono = attachmentRepository.findAllByTask(task)
                .map(AttachmentResponse::new)
                .collectList();

        return Mono.just(new TaskResponse(task))
                .zipWith(attachmentResponsesMono, ((taskResponse, attachmentResponses) -> {
                    taskResponse.setAttachments(attachmentResponses);
                    return taskResponse;
                }))
                .doOnError(throwable -> log.error(throwable.getMessage()));
    }

    protected Mono<Task> deleteAttachmentsByTask(Task task) {
        return attachmentRepository.deleteAll(attachmentRepository.findAllByTask(task))
                .doOnError(throwable -> log.error(throwable.getMessage()))
                .doOnSuccess(voidValue -> log.info("Attachments related to task '{}' deleted", task.getId()))
                .thenReturn(task);
    }

    protected Flux<Project> getAuthorizedProjects(ServerWebExchange exchange) {
        int authId = this.getAuthId(exchange);

        return projectRepository.findAll()
                .filter(project -> project.getCreatedBy() == authId || (project.getUserIds() != null && project.getUserIds().contains(authId)));
    }

    protected void deleteTemporaryAttachmentFile(ExchangeAttachment exchangeAttachment) {
        String attachmentFolderDirectory = System.getProperty("user.dir") + File.separator + UPLOADS_TMP + File.separator + ATTACHMENT_FOLDER;
        String folderDirectory = attachmentFolderDirectory + File.separator + exchangeAttachment.getTaskId();
        String filePath = folderDirectory + File.separator + exchangeAttachment.getFilename();

        File file = new File(filePath);

        boolean isDeleted = file.delete();
        if (isDeleted) {
            log.info("Temporary file '{}' has been deleted", filePath);
        } else {
            log.info("Temporary file '{}' doesn't exist", filePath);
        }
    }
}
