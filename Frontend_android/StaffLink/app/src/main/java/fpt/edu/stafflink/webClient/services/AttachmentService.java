package fpt.edu.stafflink.webClient.services;

import android.content.Context;

import fpt.edu.stafflink.models.responseDtos.AttachmentResponse;
import fpt.edu.stafflink.pagination.MultiValuePagination;
import reactor.core.publisher.Flux;

public interface AttachmentService {
    Flux<AttachmentResponse> getAttachmentsByTaskId(Context context, String taskId, MultiValuePagination pagination);
}
