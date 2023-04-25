package fpt.edu.stafflink.webClient.services.impls;

import android.content.Context;

import fpt.edu.stafflink.R;
import fpt.edu.stafflink.models.responseDtos.CommentResponse;
import fpt.edu.stafflink.pagination.MultiValuePagination;
import fpt.edu.stafflink.webClient.WebClientManager;
import fpt.edu.stafflink.webClient.services.CommentService;
import reactor.core.publisher.Flux;

public class CommentServiceImpl implements CommentService {
    @Override
    public Flux<CommentResponse> getCommentsByTaskId(Context context, String taskId, MultiValuePagination pagination) {
        return WebClientManager.getWebclientInstance(context)
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(context.getString(R.string.comments_by_task_path) + "/" + taskId)
                        .queryParams(pagination)
                        .build())
                .retrieve()
                .bodyToFlux(CommentResponse.class);
    }
}
