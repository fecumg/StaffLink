package fpt.edu.stafflink.retrofit.services;

import fpt.edu.stafflink.models.responseDtos.UserResponse;
import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface AuthenticationService {
    @POST("/login")
    Observable<Response<Object>> login(@Body RequestBody loginRequestBody);

    @GET("/auth")
    Observable<Response<Object>> getAuthUser();

    @PUT("/auth/editPersonalInfo")
    Observable<Response<UserResponse>> editPersonalInfo(@Body RequestBody editUserRequestBody);

    @GET("/auth/authorizedFunctions")
    Observable<Response<Object>> getAuthorizedFunctions();
}
