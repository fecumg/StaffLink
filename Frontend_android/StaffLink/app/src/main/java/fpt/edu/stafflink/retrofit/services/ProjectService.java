package fpt.edu.stafflink.retrofit.services;

import fpt.edu.stafflink.pagination.Pagination;
import io.reactivex.Observable;
import okhttp3.RequestBody;
import reactor.core.publisher.Flux;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

public interface ProjectService {

    @GET("/projects")
    Observable<Response<Object>> getProjects(@QueryMap Pagination pagination);

    @GET("/projects/{id}")
    Observable<Response<Object>> getProject(@Path("id") String id);

    @POST("/projects/new")
    Observable<Response<Object>> newProject(@Body RequestBody projectRequestBody);

    @PUT("/projects/edit/{id}")
    Observable<Response<Object>> editProject(@Path("id") String id, @Body RequestBody projectRequestBody);

    @GET("/projects/created")
    Observable<Response<Object>> getCreatedProjects(@QueryMap Pagination pagination);

    @GET("/projects/created")
    Flux<Response<Object>> getCreated(@QueryMap Pagination pagination);
}
