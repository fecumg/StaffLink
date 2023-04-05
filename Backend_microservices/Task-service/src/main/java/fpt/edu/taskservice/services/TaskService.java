package fpt.edu.taskservice.services;

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
    Flux<TaskResponse> getAll(Pagination pagination);
    Mono<TaskResponse> get(String id);
    Mono<Void> delete(String id);
}
