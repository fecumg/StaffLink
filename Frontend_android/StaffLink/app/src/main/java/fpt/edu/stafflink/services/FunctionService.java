package fpt.edu.stafflink.services;

import java.util.List;

import fpt.edu.stafflink.models.responseDtos.FunctionResponse;
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

public interface FunctionService {
    @GET("/functions")
    Observable<Response<Object>> getAllFunctions();

    @GET("/functions/{id}")
    Observable<Response<Object>> getFunction(@Path("id") int id);

    @POST("/functions/new")
    Observable<Response<Object>> newFunction(@Body RequestBody functionRequestBody);

    @PUT("functions/edit/{id}")
    Observable<Response<Object>> editFunction(@Path("id") int id, @Body RequestBody functionRequestBody);

    @DELETE("/functions/delete/{id}")
    Observable<Response<Object>> deleteFunction(@Path("id") int id);

    @GET("/functions/authorized")
    Observable<Response<Object>> getAuthorizedFunctions();

    @GET("/functions/potentialParents/{id}")
    Observable<Response<Object>> getPotentialParents(@Path("id") int id);
}
