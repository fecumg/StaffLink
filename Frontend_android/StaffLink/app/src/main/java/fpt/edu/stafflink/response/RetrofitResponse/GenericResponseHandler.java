package fpt.edu.stafflink.response.RetrofitResponse;

public interface GenericResponseHandler<T> {
    void handle(T responseBody);
}
