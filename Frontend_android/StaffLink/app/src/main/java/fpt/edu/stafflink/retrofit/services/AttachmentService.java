package fpt.edu.stafflink.retrofit.services;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface AttachmentService {
    @POST("/attachments/new")
    Observable<Response<Object>> newAttachment(@Body RequestBody attachmentRequestBody);

    @DELETE("/attachments/delete/{id}")
    Observable<Response<Void>> deleteAttachment(@Path("id") String id);
}
