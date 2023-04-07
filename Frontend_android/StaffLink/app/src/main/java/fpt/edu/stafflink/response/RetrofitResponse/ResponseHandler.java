package fpt.edu.stafflink.response.RetrofitResponse;

import com.google.gson.Gson;

public interface ResponseHandler {
    void handle(Object responseBody, Gson gson);
}
