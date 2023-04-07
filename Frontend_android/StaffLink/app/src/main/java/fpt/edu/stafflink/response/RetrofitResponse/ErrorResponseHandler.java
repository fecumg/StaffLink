package fpt.edu.stafflink.response.RetrofitResponse;

import com.google.gson.Gson;

import fpt.edu.stafflink.response.ErrorApiResponse;
import retrofit2.Response;

public interface ErrorResponseHandler {
    void handle(ErrorApiResponse errorApiResponse);
}
