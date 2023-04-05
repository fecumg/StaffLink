package fpt.edu.taskservice.controllers;

import fpt.edu.taskservice.dtos.requestDtos.EditTaskRequest;
import fpt.edu.taskservice.dtos.requestDtos.NewTaskRequest;
import fpt.edu.taskservice.dtos.responseDtos.TaskResponse;
import fpt.edu.taskservice.pagination.Pagination;
import fpt.edu.taskservice.services.TaskService;
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
@RequestMapping("/tasks")
public class TaskController extends BaseController {

    @Autowired
    private TaskService taskService;

    @PostMapping(value = "/new", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Mono<TaskResponse>> newTask(@ModelAttribute NewTaskRequest newTaskRequest, ServerWebExchange exchange) {
        return ResponseEntity.ok(taskService.save(newTaskRequest, exchange));
    }

    @PutMapping(value = "/edit/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Mono<TaskResponse>> editTask(@PathVariable("id") String id, @ModelAttribute EditTaskRequest editTaskRequest, ServerWebExchange exchange) {
        return ResponseEntity.ok(taskService.update(id, editTaskRequest, exchange));
    }

    @GetMapping("")
    public ResponseEntity<Flux<TaskResponse>> getTasks(@Nullable @ModelAttribute Pagination pagination) {
        return ResponseEntity.ok(taskService.getAll(pagination));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mono<TaskResponse>> getTask(@PathVariable("id") String id) {
        return ResponseEntity.ok(taskService.get(id));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Mono<Void>> deleteTask(@PathVariable("id") String id) {
        return ResponseEntity.ok(taskService.delete(id));
    }
}
