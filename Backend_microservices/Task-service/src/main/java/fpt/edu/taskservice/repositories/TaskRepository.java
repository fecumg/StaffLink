package fpt.edu.taskservice.repositories;

import fpt.edu.taskservice.entities.Project;
import fpt.edu.taskservice.entities.Task;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Truong Duc Duong
 */
public interface TaskRepository extends ReactiveMongoRepository<Task, String> {
    Flux<Task> findAllByProject(Project project);

    Mono<Long> countTasksByStatus(int status);
}
