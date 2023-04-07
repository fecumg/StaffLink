package fpt.edu.stafflink.retrofit.services;


import java.util.List;

import fpt.edu.stafflink.models.responseDtos.UserResponse;
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
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface UserService {

    @GET("/users")
    Observable<Response<Object>> getUsers(@QueryMap Pagination pagination);

    @GET("users/{id}")
    Observable<Response<Object>> getUser(@Path("id") int id);

    @POST("/users/new")
    Observable<Response<Object>> newUser(@Body RequestBody newUserRequestBody);

    @PUT("/users/edit/{id}")
    Observable<Response<Object>> editUser(@Path("id") int id, @Body RequestBody editUserRequestBody);

    @DELETE("/users/delete/{id}")
    Observable<Response<Object>> deleteUser(@Path("id") int id);

    @GET("/users/auth")
    Observable<Response<Object>> getAuthUser();

    @PUT("/users/editPersonalInfo")
    Observable<Response<Object>> editPersonalInfo(@Body RequestBody editUserRequestBody);
}
