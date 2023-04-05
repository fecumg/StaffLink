package fpt.edu.taskservice.services.impls;

import fpt.edu.taskservice.dtos.requestDtos.ProjectRequest;
import fpt.edu.taskservice.dtos.responseDtos.ProjectResponse;
import fpt.edu.taskservice.dtos.responseDtos.TaskResponse;
import fpt.edu.taskservice.entities.Project;
import fpt.edu.taskservice.pagination.Pagination;
import fpt.edu.taskservice.repositories.ProjectRepository;
import fpt.edu.taskservice.repositories.TaskRepository;
import fpt.edu.taskservice.services.ProjectService;
import fpt.edu.taskservice.services.validations.ValidationHandler;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

/**
 * @author Truong Duc Duong
 */

@Service
@Transactional(rollbackFor = Exception.class)
@Log4j2
public class ProjectServiceImpl extends BaseService<Project> implements ProjectService {
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private ValidationHandler validationHandler;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public Mono<ProjectResponse> save(ProjectRequest projectRequest, ServerWebExchange exchange) {
        validationHandler.validate(projectRequest);

        return Mono.just(projectRequest)
                .map(request -> modelMapper.map(request, Project.class))
                .map(preparedProject -> {
                    super.setCreatedBy(preparedProject, exchange);
                    return preparedProject;
                })
                .flatMap(preparedProject -> projectRepository.save(preparedProject))
                .flatMap(this::buildProjectResponse)
                .doOnSuccess(projectResponse -> log.info("Project with id '{}' saved successfully", projectResponse.getId()))
                .doOnError(throwable -> log.error(throwable.getMessage()));
    }

    @Override
    public Mono<ProjectResponse> update(String id, ProjectRequest projectRequest, ServerWebExchange exchange) {
        validationHandler.validate(projectRequest);

        return projectRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Project not found")))
                .zipWith(Mono.just(projectRequest), ((project, request) -> {
                    Project preparedProject = modelMapper.map(request, Project.class);
                    super.setUpdatedBy(preparedProject, exchange);
                    preparedProject.setId(id);
                    return preparedProject;
                }))
                .flatMap(preparedProject -> projectRepository.save(preparedProject))
                .flatMap(this::buildProjectResponse)
                .doOnSuccess(projectResponse -> log.info("Project with id '{}' updated successfully", projectResponse.getId()))
                .doOnError(throwable -> log.error(throwable.getMessage()));
    }

    @Override
    public Flux<ProjectResponse> getAll(Pagination pagination) {
        Flux<Project> projectFlux;
        if (pagination == null) {
            projectFlux = projectRepository.findAll();
        } else {
            projectFlux = super.paginate(projectRepository.findAll(), pagination);
        }
        return projectFlux
                .map(ProjectResponse::new)
                .delayElements(Duration.ofMillis(100))
                .doOnError(throwable -> log.error(throwable.getMessage()));
    }

    @Override
    public Mono<ProjectResponse> get(String id) {
        return projectRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Project not found")))
                .flatMap(this::buildProjectResponse)
                .doOnError(throwable -> log.error(throwable.getMessage()));
    }

    @Override
    public Mono<Void> delete(String id) {
        return projectRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Project not found")))
                .flatMap(this::deleteTasksByProject)
                .flatMap(project -> projectRepository.delete(project))
                .doOnSuccess(voidValue -> log.info("Project with id '{}' deleted", id))
                .doOnError(throwable -> log.error(throwable.getMessage()));
    }

    private Mono<Project> deleteTasksByProject(Project project) {
        return taskRepository.deleteAll(
                        taskRepository.findAllByProject(project)
                                .flatMap(super::deleteAttachmentsByTask)
                )
                .doOnError(throwable -> log.error(throwable.getMessage()))
                .doOnSuccess(voidValue -> log.info("Tasks related to project '{}' deleted", project.getId()))
                .thenReturn(project);
    }

    private Mono<ProjectResponse> buildProjectResponse(Project project) {
        Mono<List<TaskResponse>> taskResponsesMono = taskRepository.findAllByProject(project)
                .flatMap(super::buildTaskResponse)
                .collectList();

        return Mono.just(new ProjectResponse(project))
                .zipWith(taskResponsesMono, ((projectResponse, taskResponses) -> {
                    projectResponse.setTasks(taskResponses);
                    return projectResponse;
                }))
                .doOnError(throwable -> log.error(throwable.getMessage()));
    }
}
