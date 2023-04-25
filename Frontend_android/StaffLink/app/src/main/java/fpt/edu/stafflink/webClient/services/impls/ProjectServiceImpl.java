package fpt.edu.stafflink.webClient.services.impls;

import android.content.Context;

import fpt.edu.stafflink.R;
import fpt.edu.stafflink.models.responseDtos.ProjectResponse;
import fpt.edu.stafflink.pagination.MultiValuePagination;
import fpt.edu.stafflink.pagination.Pagination;
import fpt.edu.stafflink.webClient.WebClientManager;
import fpt.edu.stafflink.webClient.services.ProjectService;
import reactor.core.publisher.Flux;

public class ProjectServiceImpl implements ProjectService {


    @Override
    public Flux<ProjectResponse> getProjects(Context context, MultiValuePagination pagination) {
        return WebClientManager.getWebclientInstance(context)
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(context.getString(R.string.projects_path))
                        .queryParams(pagination)
                        .build())
                .retrieve()
                .bodyToFlux(ProjectResponse.class);
    }

    @Override
    public Flux<ProjectResponse> getAssignedProjects(Context context, MultiValuePagination pagination) {
        return WebClientManager.getWebclientInstance(context)
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(context.getString(R.string.assigned_projects_path))
                        .queryParams(pagination)
                        .build())
                .retrieve()
                .bodyToFlux(ProjectResponse.class);
    }

    @Override
    public Flux<ProjectResponse> getAuthorizedProjects(Context context, MultiValuePagination pagination) {
        return WebClientManager.getWebclientInstance(context)
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(context.getString(R.string.authorized_projects_path))
                        .queryParams(pagination)
                        .build())
                .retrieve()
                .bodyToFlux(ProjectResponse.class);
    }
}
