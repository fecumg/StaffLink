package fpt.edu.stafflink.webClient.services;

import android.content.Context;

import fpt.edu.stafflink.models.responseDtos.ProjectResponse;
import fpt.edu.stafflink.pagination.MultiValuePagination;
import fpt.edu.stafflink.pagination.Pagination;
import reactor.core.publisher.Flux;

public interface ProjectService {
    Flux<ProjectResponse> getProjects(Context context, MultiValuePagination pagination);
    Flux<ProjectResponse> getAssignedProjects(Context context, MultiValuePagination pagination);
    Flux<ProjectResponse> getAuthorizedProjects(Context context, MultiValuePagination pagination);
}
