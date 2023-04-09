package fpt.edu.stafflink.webClient.services;

import android.content.Context;

import fpt.edu.stafflink.models.requestDtos.ProjectRequest;
import fpt.edu.stafflink.models.responseDtos.ProjectResponse;
import fpt.edu.stafflink.pagination.MultiValuePagination;
import fpt.edu.stafflink.pagination.Pagination;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProjectService {
    Flux<Object> getProjects(Context context, Pagination pagination);
    Flux<Object> getAssignedProjects(Context context, Pagination pagination);
    Flux<ProjectResponse> getCreatedProjects(Context context, MultiValuePagination pagination);
}
