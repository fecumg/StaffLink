package fpt.edu.taskservice.repositories;

import fpt.edu.taskservice.entities.Attachment;
import fpt.edu.taskservice.entities.Task;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

/**
 * @author Truong Duc Duong
 */
public interface AttachmentRepository extends ReactiveMongoRepository<Attachment, String> {

    Flux<Attachment> findAllByTask(Task task);
}
