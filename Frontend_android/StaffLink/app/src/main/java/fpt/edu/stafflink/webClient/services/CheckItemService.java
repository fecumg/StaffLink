package fpt.edu.stafflink.webClient.services;

import android.content.Context;

import fpt.edu.stafflink.models.responseDtos.CheckItemResponse;
import fpt.edu.stafflink.pagination.MultiValuePagination;
import reactor.core.publisher.Flux;

public interface CheckItemService {
    Flux<CheckItemResponse> getChecklistByTaskId(Context context, String taskId, MultiValuePagination pagination);
}
