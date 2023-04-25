package fpt.edu.stafflink.webClient.services.impls;

import android.content.Context;

import fpt.edu.stafflink.R;
import fpt.edu.stafflink.models.responseDtos.CheckItemResponse;
import fpt.edu.stafflink.pagination.MultiValuePagination;
import fpt.edu.stafflink.webClient.WebClientManager;
import fpt.edu.stafflink.webClient.services.CheckItemService;
import reactor.core.publisher.Flux;

public class CheckItemServiceImpl implements CheckItemService {
    @Override
    public Flux<CheckItemResponse> getChecklistByTaskId(Context context, String taskId, MultiValuePagination pagination) {
        return WebClientManager.getWebclientInstance(context)
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(context.getString(R.string.checklist_by_task_path) + "/" + taskId)
                        .queryParams(pagination)
                        .build())
                .retrieve()
                .bodyToFlux(CheckItemResponse.class);
    }
}
