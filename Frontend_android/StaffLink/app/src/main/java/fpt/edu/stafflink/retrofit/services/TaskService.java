package fpt.edu.stafflink.retrofit.services;

import fpt.edu.stafflink.models.responseDtos.TaskResponse;
import fpt.edu.stafflink.pagination.Pagination;
import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

public interface TaskService {
    @GET("/tasks")
    Observable<Response<Object>> getTasks(@QueryMap Pagination pagination);

    @GET("/tasks/{id}")
    Observable<Response<Object>> getTask(@Path("id") String id);

    @POST("/tasks/new")
    Observable<Response<Object>> newTask(@Body RequestBody newTaskRequestBody);

    @PUT("/tasks/edit/{id}")
    Observable<Response<TaskResponse>> editTask(@Path("id") String id, @Body RequestBody editTaskRequestBody);

    @PUT("/tasks/status/{id}")
    Observable<Response<TaskResponse>> editStatus(@Path("id") String id, @Body RequestBody editStatusRequestBody);
}
