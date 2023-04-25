package fpt.edu.taskservice.controllers;

import fpt.edu.taskservice.dtos.responseDtos.CommentResponse;
import fpt.edu.taskservice.pagination.Pagination;
import fpt.edu.taskservice.services.CommentService;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Truong Duc Duong
 */

@CrossOrigin
@RestController
@RequestMapping("/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/by/{taskId}")
    public ResponseEntity<Flux<CommentResponse>> getCommentsByTaskId(@PathVariable("taskId") String taskId, @Nullable @ModelAttribute Pagination pagination) {
        return ResponseEntity.ok(commentService.getAllByTaskId(taskId, pagination));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Mono<Void>> deleteComment(@PathVariable("id") String id) {
        return ResponseEntity.ok(commentService.delete(id));
    }
}
