package fpt.edu.taskservice.controllers;

import fpt.edu.taskservice.dtos.requestDtos.EditCheckItemRequest;
import fpt.edu.taskservice.dtos.requestDtos.NewCheckItemRequest;
import fpt.edu.taskservice.dtos.requestDtos.RearrangedCheckItemRequest;
import fpt.edu.taskservice.dtos.responseDtos.CheckItemResponse;
import fpt.edu.taskservice.pagination.Pagination;
import fpt.edu.taskservice.services.CheckItemService;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author Truong Duc Duong
 */

@CrossOrigin
@RestController
@RequestMapping("/checklist")
public class CheckItemController extends BaseController {

    @Autowired
    private CheckItemService checkItemService;

    @PostMapping(value = "/new", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Mono<CheckItemResponse>> newCheckItem(@ModelAttribute NewCheckItemRequest newCheckItemRequest, ServerWebExchange exchange) {
        return ResponseEntity.ok(checkItemService.save(newCheckItemRequest, exchange));
    }

    @PutMapping(value = "/edit/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Mono<CheckItemResponse>> editCheckItem(@PathVariable("id") String id, @ModelAttribute EditCheckItemRequest editCheckItemRequest) {
        return ResponseEntity.ok(checkItemService.update(id, editCheckItemRequest));
    }

    @GetMapping("/by/{taskId}")
    public ResponseEntity<Flux<CheckItemResponse>> getCheckItemsByTaskId(@PathVariable("taskId") String taskId, @Nullable @ModelAttribute Pagination pagination) {
        return ResponseEntity.ok(checkItemService.getAllByTaskId(taskId, pagination));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Mono<Void>> deleteCheckItem(@PathVariable("id") String id) {
        return ResponseEntity.ok(checkItemService.delete(id));
    }

    @PutMapping("/rearrange")
    public ResponseEntity<Mono<Void>> rearrangeCheckList(@RequestBody List<RearrangedCheckItemRequest> rearrangedCheckItemRequests) {
        return ResponseEntity.ok(checkItemService.rearrange(rearrangedCheckItemRequests));
    }
}
