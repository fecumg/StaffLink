package fpt.edu.stafflink.webClient.services.impls;

import android.content.Context;

import fpt.edu.stafflink.models.requestDtos.ProjectRequest;
import fpt.edu.stafflink.models.responseDtos.ProjectResponse;
import fpt.edu.stafflink.pagination.MultiValuePagination;
import fpt.edu.stafflink.pagination.Pagination;
import fpt.edu.stafflink.webClient.WebClientManager;
import fpt.edu.stafflink.webClient.services.ProjectService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ProjectServiceImpl implements ProjectService {

    @Override
    public Flux<Object> getProjects(Pagination pagination) {
        return null;
    }

    @Override
    public Mono<Object> getProject(String id) {
        return null;
    }

    @Override
    public Mono<Object> newProject(ProjectRequest projectRequest) {
        return null;
    }

    @Override
    public Mono<Object> editProject(String id, ProjectRequest projectRequest) {
        return null;
    }

    @Override
    public Flux<ProjectResponse> getCreatedProjects(Context context, MultiValuePagination pagination) {
        return WebClientManager.getWebclientInstance(context)
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/projects/created")
                        .queryParams(pagination)
                        .build())
                .retrieve()
                .bodyToFlux(ProjectResponse.class);
    }
}
