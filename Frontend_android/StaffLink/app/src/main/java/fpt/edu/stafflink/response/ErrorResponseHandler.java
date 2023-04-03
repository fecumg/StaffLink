package fpt.edu.stafflink.response;

import com.google.gson.Gson;

import retrofit2.Response;

public interface ErrorResponseHandler {
    void handle(ErrorApiResponse errorApiResponse);
}
