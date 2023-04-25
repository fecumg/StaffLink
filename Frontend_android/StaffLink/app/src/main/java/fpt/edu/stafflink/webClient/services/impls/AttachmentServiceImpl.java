package fpt.edu.stafflink.webClient.services.impls;

import android.content.Context;

import fpt.edu.stafflink.R;
import fpt.edu.stafflink.models.responseDtos.AttachmentResponse;
import fpt.edu.stafflink.pagination.MultiValuePagination;
import fpt.edu.stafflink.webClient.WebClientManager;
import fpt.edu.stafflink.webClient.services.AttachmentService;
import reactor.core.publisher.Flux;

public class AttachmentServiceImpl implements AttachmentService {
    @Override
    public Flux<AttachmentResponse> getAttachmentsByTaskId(Context context, String taskId, MultiValuePagination pagination) {
        return WebClientManager.getWebclientInstance(context)
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(context.getString(R.string.attachments_by_task_path) + "/" + taskId)
                        .queryParams(pagination)
                        .build())
                .retrieve()
                .bodyToFlux(AttachmentResponse.class);
    }
}
