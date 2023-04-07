package fpt.edu.stafflink.retrofit.services;

import java.util.List;

import fpt.edu.stafflink.models.responseDtos.RoleResponse;
import fpt.edu.stafflink.models.responseDtos.UserResponse;
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

public interface RoleService {
    @GET("/roles")
    Observable<Response<Object>> getRoles(@QueryMap Pagination pagination);

    @GET("/roles/{id}")
    Observable<Response<Object>> getRole(@Path("id") int id);

    @POST("/roles/new")
    Observable<Response<Object>> newRole(@Body RequestBody roleRequestBody);

    @PUT("/roles/edit/{id}")
    Observable<Response<Object>> editRole(@Path("id") int id, @Body RequestBody roleRequestBody);
}
