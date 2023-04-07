package fpt.edu.stafflink.retrofit.services;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthenticationService {
    @POST("/login")
    Observable<Response<Object>> login(@Body RequestBody loginRequestBody);
}
