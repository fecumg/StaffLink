package fpt.edu.taskservice.repositories;

import fpt.edu.taskservice.entities.Project;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

/**
 * @author Truong Duc Duong
 */
public interface ProjectRepository extends ReactiveMongoRepository<Project, String> {
}
