package fpt.edu.stafflink.retrofit.services;

import java.util.List;

import fpt.edu.stafflink.models.requestDtos.checkItemDtos.RearrangedCheckItemRequest;
import fpt.edu.stafflink.models.responseDtos.CheckItemResponse;
import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface CheckItemService {
    @POST("/checklist/new")
    Observable<Response<CheckItemResponse>> newCheckItem(@Body RequestBody newCheckItemRequestBody);

    @PUT("/checklist/edit/{id}")
    Observable<Response<CheckItemResponse>> editCheckItem(@Path("id") String id, @Body RequestBody editCheckItemRequestBody);

    @PUT("/checklist/rearrange")
    Observable<Response<Void>> rearrangeCheckList(@Body List<RearrangedCheckItemRequest> rearrangedCheckItemRequests);

    @DELETE("/checklist/delete/{id}")
    Observable<Response<Void>> deleteCheckItem(@Path("id") String id);
}
