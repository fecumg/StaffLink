package fpt.edu.taskservice.repositories;

import fpt.edu.taskservice.entities.Comment;
import fpt.edu.taskservice.entities.Task;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

/**
 * @author Truong Duc Duong
 */
public interface CommentRepository extends ReactiveMongoRepository<Comment, String> {
    Flux<Comment> findAllByTask(Task task);
}
