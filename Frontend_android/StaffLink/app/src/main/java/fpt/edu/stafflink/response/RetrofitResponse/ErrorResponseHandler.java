package fpt.edu.stafflink.response.RetrofitResponse;

import fpt.edu.stafflink.response.ErrorApiResponse;

public interface ErrorResponseHandler {
    void handle(ErrorApiResponse errorApiResponse);
}
