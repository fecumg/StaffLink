package fpt.edu.taskservice.services;

import fpt.edu.taskservice.dtos.requestDtos.ProjectRequest;
import fpt.edu.taskservice.dtos.responseDtos.ProjectResponse;
import fpt.edu.taskservice.pagination.Pagination;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Truong Duc Duong
 */

@Service
public interface ProjectService {
    Mono<ProjectResponse> save(ProjectRequest projectRequest, ServerWebExchange exchange);
    Mono<ProjectResponse> update(String id, ProjectRequest projectRequest, ServerWebExchange exchange);
    Flux<ProjectResponse> getAll(Pagination pagination);
    Mono<ProjectResponse> get(String id);
    Mono<Void> delete(String id);
    Flux<ProjectResponse> getAuthorizedProjects(Pagination pagination, ServerWebExchange exchange);
    Flux<ProjectResponse> getAssignedProjects(Pagination pagination, ServerWebExchange exchange);
}
