package fpt.edu.taskservice.services;

import fpt.edu.taskservice.dtos.requestDtos.EditCheckItemRequest;
import fpt.edu.taskservice.dtos.requestDtos.NewCheckItemRequest;
import fpt.edu.taskservice.dtos.requestDtos.RearrangedCheckItemRequest;
import fpt.edu.taskservice.dtos.responseDtos.CheckItemResponse;
import fpt.edu.taskservice.pagination.Pagination;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author Truong Duc Duong
 */

@Service
public interface CheckItemService {
    Mono<CheckItemResponse> save(NewCheckItemRequest newCheckItemRequest, ServerWebExchange exchange);
    Mono<CheckItemResponse> update(String id, EditCheckItemRequest editCheckItemRequest);
    Flux<CheckItemResponse> getAllByTaskId(String taskId, Pagination pagination);
    Mono<Void> delete(String id);
    Mono<Void> rearrange(List<RearrangedCheckItemRequest> rearrangedCheckItemRequests);
}
