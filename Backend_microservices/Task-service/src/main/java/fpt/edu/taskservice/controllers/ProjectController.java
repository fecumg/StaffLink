package fpt.edu.taskservice.controllers;

import fpt.edu.taskservice.dtos.requestDtos.ProjectRequest;
import fpt.edu.taskservice.dtos.responseDtos.ProjectResponse;
import fpt.edu.taskservice.pagination.Pagination;
import fpt.edu.taskservice.services.ProjectService;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Truong Duc Duong
 */

@CrossOrigin
@RestController
@RequestMapping("/projects")
public class ProjectController extends BaseController {
    @Autowired
    private ProjectService projectService;

    @PostMapping(value = "/new", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Mono<ProjectResponse>> newProject(@ModelAttribute ProjectRequest projectRequest, ServerWebExchange exchange) {
        return ResponseEntity.ok(projectService.save(projectRequest, exchange));
    }

    @PutMapping(value = "/edit/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Mono<ProjectResponse>> editProject(@PathVariable("id") String id, @ModelAttribute ProjectRequest projectRequest, ServerWebExchange exchange) {
        return ResponseEntity.ok(projectService.update(id, projectRequest, exchange));
    }

    @GetMapping("")
    public ResponseEntity<Flux<ProjectResponse>> getProjects(@Nullable @ModelAttribute Pagination pagination) {
        return ResponseEntity.ok(projectService.getAll(pagination));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mono<ProjectResponse>> getProject(@PathVariable("id") String id) {
        return ResponseEntity.ok(projectService.get(id));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Mono<Void>> deleteProject(@PathVariable("id") String id) {
        return ResponseEntity.ok(projectService.delete(id));
    }

    @GetMapping("/authorized")
    public ResponseEntity<Flux<ProjectResponse>> getAuthorizedProjects(@Nullable @ModelAttribute Pagination pagination, ServerWebExchange exchange) {
        return ResponseEntity.ok(projectService.getAuthorizedProjects(pagination, exchange));
    }

    @GetMapping("/assigned")
    public ResponseEntity<Flux<ProjectResponse>> getAssignedProjects(@Nullable @ModelAttribute Pagination pagination, ServerWebExchange exchange) {
        return ResponseEntity.ok(projectService.getAssignedProjects(pagination, exchange));
    }
}
