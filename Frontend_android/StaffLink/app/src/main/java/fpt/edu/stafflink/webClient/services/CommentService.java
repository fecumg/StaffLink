package fpt.edu.stafflink.webClient.services;

import android.content.Context;

import fpt.edu.stafflink.models.responseDtos.CommentResponse;
import fpt.edu.stafflink.pagination.MultiValuePagination;
import reactor.core.publisher.Flux;

public interface CommentService {
    Flux<CommentResponse> getCommentsByTaskId(Context context, String taskId, MultiValuePagination pagination);

}
