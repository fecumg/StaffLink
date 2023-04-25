package fpt.edu.taskservice.services;

import fpt.edu.taskservice.dtos.responseDtos.CommentResponse;
import fpt.edu.taskservice.pagination.Pagination;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Truong Duc Duong
 */

@Service
public interface CommentService {
    Mono<String> save(String payload, WebSocketSession webSocketSession);
    Flux<CommentResponse> getAllByTaskId(String taskId, Pagination pagination);
    Mono<Void> delete(String id);
}
