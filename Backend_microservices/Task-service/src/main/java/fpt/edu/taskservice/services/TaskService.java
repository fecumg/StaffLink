package fpt.edu.taskservice.services;

import fpt.edu.taskservice.dtos.requestDtos.EditStatusRequest;
import fpt.edu.taskservice.dtos.requestDtos.EditTaskRequest;
import fpt.edu.taskservice.dtos.requestDtos.NewTaskRequest;
import fpt.edu.taskservice.dtos.responseDtos.TaskResponse;
import fpt.edu.taskservice.pagination.Pagination;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Truong Duc Duong
 */

@Service
public interface TaskService {
    Mono<TaskResponse> save(NewTaskRequest newTaskRequest, ServerWebExchange exchange);
    Mono<TaskResponse> update(String id, EditTaskRequest editTaskRequest, ServerWebExchange exchange);
    Mono<TaskResponse> updateStatus(String id, EditStatusRequest editStatusRequest, ServerWebExchange exchange);
    Flux<TaskResponse> getAll(Pagination pagination);
    Mono<TaskResponse> get(String id);
    Mono<Void> delete(String id);

    Flux<TaskResponse> getTasks(int status, Pagination pagination);
    Flux<TaskResponse> getTasksByProject(String projectId, int status, Pagination pagination);
    Flux<TaskResponse> getAssignedTasks(int status, Pagination pagination, ServerWebExchange exchange);
    Flux<TaskResponse> getAssignedTasksByProject(String projectId, int status, Pagination pagination, ServerWebExchange exchange);
    Flux<TaskResponse> getAuthorizedTasks(int status, Pagination pagination, ServerWebExchange exchange);
    Flux<TaskResponse> getAuthorizedTasksByProjectId(String projectId, int status, Pagination pagination, ServerWebExchange exchange);
}
