package fpt.edu.stafflink.response;

import com.google.gson.Gson;

public interface MergedResponseHandler {
    void handle(Object firstResBody, Object secondResBody, Gson gson);
}
