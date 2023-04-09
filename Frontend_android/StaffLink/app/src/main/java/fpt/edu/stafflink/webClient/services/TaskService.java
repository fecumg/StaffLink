package fpt.edu.stafflink.webClient.services;

import android.content.Context;

import fpt.edu.stafflink.models.responseDtos.TaskResponse;
import fpt.edu.stafflink.pagination.MultiValuePagination;
import reactor.core.publisher.Flux;

public interface TaskService {

    Flux<TaskResponse> getAuthorizedTasks(Context context, int taskStatusCode, MultiValuePagination pagination);
    Flux<TaskResponse> getAuthorizedTasksByProject(Context context, String projectId, int taskStatusCode, MultiValuePagination pagination);

    Flux<TaskResponse> getAssignedTasks(Context context, int taskStatusCode, MultiValuePagination pagination);
    Flux<TaskResponse> getAssignedTasksByProject(Context context, String projectId, int taskStatusCode, MultiValuePagination pagination);

    Flux<TaskResponse> getTasks(Context context, int taskStatusCode, MultiValuePagination pagination);
    Flux<TaskResponse> getTasksByProject(Context context, String projectId, int taskStatusCode, MultiValuePagination pagination);
}
