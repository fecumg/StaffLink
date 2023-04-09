package fpt.edu.stafflink.webClient.services.impls;

import android.content.Context;

import fpt.edu.stafflink.R;
import fpt.edu.stafflink.models.responseDtos.TaskResponse;
import fpt.edu.stafflink.pagination.MultiValuePagination;
import fpt.edu.stafflink.webClient.WebClientManager;
import fpt.edu.stafflink.webClient.services.TaskService;
import reactor.core.publisher.Flux;

public class TaskServiceImpl implements TaskService {
    @Override
    public Flux<TaskResponse> getAuthorizedTasks(Context context, int taskStatusCode, MultiValuePagination pagination) {
        return WebClientManager.getWebclientInstance(context)
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(context.getString(R.string.authorized_tasks_path))
                        .queryParam("status", taskStatusCode)
                        .queryParams(pagination)
                        .build())
                .retrieve()
                .bodyToFlux(TaskResponse.class);
    }

    @Override
    public Flux<TaskResponse> getAuthorizedTasksByProject(Context context, String projectId, int taskStatusCode, MultiValuePagination pagination) {
        return WebClientManager.getWebclientInstance(context)
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(context.getString(R.string.authorized_tasks_path) + "/" + projectId)
                        .queryParam("status", taskStatusCode)
                        .queryParams(pagination)
                        .build())
                .retrieve()
                .bodyToFlux(TaskResponse.class);
    }

    @Override
    public Flux<TaskResponse> getAssignedTasks(Context context, int taskStatusCode, MultiValuePagination pagination) {
        return WebClientManager.getWebclientInstance(context)
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(context.getString(R.string.assigned_tasks_path))
                        .queryParam("status", taskStatusCode)
                        .queryParams(pagination)
                        .build())
                .retrieve()
                .bodyToFlux(TaskResponse.class);
    }

    @Override
    public Flux<TaskResponse> getAssignedTasksByProject(Context context, String projectId, int taskStatusCode, MultiValuePagination pagination) {
        return WebClientManager.getWebclientInstance(context)
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(context.getString(R.string.assigned_tasks_path) + "/" + projectId)
                        .queryParam("status", taskStatusCode)
                        .queryParams(pagination)
                        .build())
                .retrieve()
                .bodyToFlux(TaskResponse.class);
    }

    @Override
    public Flux<TaskResponse> getTasks(Context context, int taskStatusCode, MultiValuePagination pagination) {
        return WebClientManager.getWebclientInstance(context)
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(context.getString(R.string.tasks_path))
                        .queryParam("status", taskStatusCode)
                        .queryParams(pagination)
                        .build())
                .retrieve()
                .bodyToFlux(TaskResponse.class);
    }

    @Override
    public Flux<TaskResponse> getTasksByProject(Context context, String projectId, int taskStatusCode, MultiValuePagination pagination) {
        return WebClientManager.getWebclientInstance(context)
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(context.getString(R.string.tasks_path) + "/" + projectId)
                        .queryParam("status", taskStatusCode)
                        .queryParams(pagination)
                        .build())
                .retrieve()
                .bodyToFlux(TaskResponse.class);
    }
}
