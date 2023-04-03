package fpt.edu.stafflink.response;

import retrofit2.Response;

public class MergedResponse {
    private final Response<Object> first;
    private final Response<Object> second;

    public MergedResponse(Response<Object> first, Response<Object> second) {
        this.first = first;
        this.second = second;
    }

    public Response<Object> getFirst() {
        return first;
    }

    public Response<Object> getSecond() {
        return second;
    }
}
