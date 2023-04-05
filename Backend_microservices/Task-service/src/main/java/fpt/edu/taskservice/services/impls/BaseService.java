package fpt.edu.taskservice.services.impls;

import fpt.edu.taskservice.dtos.responseDtos.AttachmentResponse;
import fpt.edu.taskservice.dtos.responseDtos.TaskResponse;
import fpt.edu.taskservice.entities.Task;
import fpt.edu.taskservice.pagination.Pagination;
import fpt.edu.taskservice.repositories.AttachmentRepository;
import fpt.edu.taskservice.services.validations.ValidationHandler;
import jakarta.ws.rs.BadRequestException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

    @Autowired
    private ValidationHandler validationHandler;
    @Autowired
    private AttachmentRepository attachmentRepository;

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

    protected Date parseDate(String dateString, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        try {
            return sdf.parse(dateString);
        } catch (ParseException e) {
            throw new BadRequestException("Invalid date format: " + dateString);
        }
    }

    protected <FieldType> FieldType getFieldValue(Object object, String fieldName, Class<FieldType> resultType) {
        if (StringUtils.hasText(fieldName) || object == null) {
            return null;
        }
        Class<?> clazz = object.getClass();
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value =  field.get(object);
            if (value != null) {
                return resultType.cast(value);
            }
            return null;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected Flux<T> paginate(Flux<T> objectFlux, Pagination pagination) {
        validationHandler.validate(pagination);

        Flux<T> sortedObjectFlux;
        if (pagination.getDirection().equals(Pagination.ASC)) {
            sortedObjectFlux = objectFlux.sort(Comparator.comparing(task -> this.getFieldValue(task, pagination.getSortBy(), Date.class)));
        } else if (pagination.getDirection().equals(Pagination.DESC)) {
            sortedObjectFlux = objectFlux.sort(Comparator.comparing(task -> this.getFieldValue(task, pagination.getSortBy(), Date.class)).reversed());
        } else {
            throw new BadRequestException("Sort direction must be either '" + Pagination.ASC + "' or '" + Pagination.DESC + "'");
        }

        return sortedObjectFlux
                .skip((long) (pagination.getPageNumber() - 1) * pagination.getPageSize())
                .take(pagination.getPageSize());
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
}
