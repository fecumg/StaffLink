package fpt.edu.taskservice.services.impls;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fpt.edu.taskservice.dtos.requestDtos.CommentRequest;
import fpt.edu.taskservice.dtos.responseDtos.CommentResponse;
import fpt.edu.taskservice.entities.Comment;
import fpt.edu.taskservice.pagination.Pagination;
import fpt.edu.taskservice.repositories.CommentRepository;
import fpt.edu.taskservice.repositories.TaskRepository;
import fpt.edu.taskservice.services.CommentService;
import fpt.edu.taskservice.services.validations.ValidationHandler;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * @author Truong Duc Duong
 */

@Service
@Transactional(rollbackFor = Exception.class)
@Log4j2
public class CommentServiceImpl extends BaseService<Comment> implements CommentService {
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private ValidationHandler validationHandler;

    @Override
    public Mono<String> save(String payload, WebSocketSession webSocketSession) {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .create();

        CommentRequest commentRequest = gson.fromJson(payload, CommentRequest.class);
        validationHandler.validate(commentRequest);

        return taskRepository.findById(commentRequest.getTaskId())
                .switchIfEmpty(Mono.error(new NotFoundException( "Task not found")))
                .map(task -> new Comment(commentRequest.getContent(), task))
                .map(comment -> {
                    this.setCreatedBy(comment, webSocketSession);
                    return comment;
                })
                .flatMap(comment -> commentRepository.save(comment))
                .map(CommentResponse::new)
                .doOnSuccess(commentResponse -> log.info("Comment {} has been added successfully", commentResponse.getId()))
                .map(gson::toJson)
                .doOnError(throwable -> log.error(throwable.getMessage()));
    }

    @Override
    public Flux<CommentResponse> getAllByTaskId(String taskId, Pagination pagination) {
        Flux<Comment> commentFlux = taskRepository.findById(taskId)
                .switchIfEmpty(Mono.error(new NotFoundException( "Task not found")))
                .flatMapMany(task -> commentRepository.findAllByTask(task));

        return this.buildCommentResponseFlux(commentFlux, pagination);
    }

    @Override
    public Mono<Void> delete(String id) {
        return commentRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException( "Comment not found")))
                .flatMap(comment -> commentRepository.delete(comment))
                .doOnSuccess(voidValue -> log.info("Comment with id {} has been deleted successfully", id))
                .doOnError(throwable -> log.error(throwable.getMessage()));
    }

    private Flux<CommentResponse> buildCommentResponseFlux(Flux<Comment> commentFlux, Pagination pagination) {
        return super.paginate(commentFlux, pagination)
                .map(CommentResponse::new)
                .delayElements(Duration.ofMillis(50))
                .doOnError(throwable -> log.error(throwable.getMessage()));
    }

    private void setCreatedBy(Comment comment, WebSocketSession webSocketSession) {
        String authUserIdString = webSocketSession.getHandshakeInfo().getHeaders().getFirst(AUTH_ID);
        if (StringUtils.hasText(authUserIdString)) {
            int authUserId = Integer.parseInt(authUserIdString);
            comment.setCreatedBy(authUserId);
        }
    }
}
