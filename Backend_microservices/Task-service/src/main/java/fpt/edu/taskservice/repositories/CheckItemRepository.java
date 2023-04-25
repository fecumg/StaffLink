package fpt.edu.taskservice.repositories;

import fpt.edu.taskservice.entities.CheckItem;
import fpt.edu.taskservice.entities.Task;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

/**
 * @author Truong Duc Duong
 */
public interface CheckItemRepository extends ReactiveMongoRepository<CheckItem, String> {
    Flux<CheckItem> findAllByTask(Task task);
}
