package fpt.edu.stafflink.retrofit.services;

import java.util.List;

import fpt.edu.stafflink.models.responseDtos.FunctionResponse;
import fpt.edu.stafflink.pagination.Pagination;
import io.reactivex.Observable;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

public interface FunctionService {
    @GET("/functions/all")
    Observable<Response<Object>> getFunctions(@QueryMap Pagination pagination);

    @GET("/functions/get/{id}")
    Observable<Response<Object>> getFunction(@Path("id") int id);

    @POST("/functions/new")
    Observable<Response<Object>> newFunction(@Body RequestBody functionRequestBody);

    @PUT("/functions/edit/{id}")
    Observable<Response<Object>> editFunction(@Path("id") int id, @Body RequestBody functionRequestBody);

    @DELETE("/functions/delete/{id}")
    Observable<Response<Object>> deleteFunction(@Path("id") int id);

    @GET("/functions/potentialParents/{id}")
    Observable<Response<Object>> getPotentialParents(@Path("id") int id);
}
