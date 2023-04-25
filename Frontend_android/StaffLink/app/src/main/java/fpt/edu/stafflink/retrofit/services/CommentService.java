package fpt.edu.stafflink.retrofit.services;

import fpt.edu.stafflink.models.responseDtos.CommentResponse;
import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface CommentService {
    @POST("/comments/new")
    Observable<Response<CommentResponse>> newComment(@Body RequestBody commentRequestBody);
}
