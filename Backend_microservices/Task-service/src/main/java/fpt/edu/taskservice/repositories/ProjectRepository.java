package fpt.edu.taskservice.repositories;

import fpt.edu.taskservice.entities.Project;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

/**
 * @author Truong Duc Duong
 */
public interface ProjectRepository extends ReactiveMongoRepository<Project, String> {
    Flux<Project> findAllByCreatedBy(int userId);
}
