package fpt.edu.taskservice.services.impls;

import fpt.edu.taskservice.dtos.requestDtos.EditCheckItemRequest;
import fpt.edu.taskservice.dtos.requestDtos.NewCheckItemRequest;
import fpt.edu.taskservice.dtos.requestDtos.RearrangedCheckItemRequest;
import fpt.edu.taskservice.dtos.responseDtos.CheckItemResponse;
import fpt.edu.taskservice.entities.CheckItem;
import fpt.edu.taskservice.entities.Task;
import fpt.edu.taskservice.pagination.Pagination;
import fpt.edu.taskservice.repositories.CheckItemRepository;
import fpt.edu.taskservice.repositories.TaskRepository;
import fpt.edu.taskservice.services.CheckItemService;
import fpt.edu.taskservice.services.validations.ValidationHandler;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * @author Truong Duc Duong
 */

@Service
@Transactional(rollbackFor = Exception.class)
@Log4j2
public class CheckItemServiceImpl extends BaseService<CheckItem> implements CheckItemService {

    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private CheckItemRepository checkItemRepository;
    @Autowired
    private ValidationHandler validationHandler;

    @Override
    public Mono<CheckItemResponse> save(NewCheckItemRequest newCheckItemRequest, ServerWebExchange exchange) {
        validationHandler.validate(newCheckItemRequest);

        Mono<Task> taskMono = taskRepository.findById(newCheckItemRequest.getTaskId())
                .switchIfEmpty(Mono.error(new NotFoundException( "Task not found")));

        Mono<Integer> maxPositionMono = taskMono
                .flatMapMany(task -> checkItemRepository.findAllByTask(task))
                .collectList()
                .map(checkItems -> {
                        if (checkItems.size() > 0) {
                            return Collections.max(
                                    checkItems.stream()
                                            .map(CheckItem::getPosition)
                                            .toList()
                                    );
                        } else return 0;
                })
                .onErrorReturn(0);

        return taskMono
                .map(task -> new CheckItem(newCheckItemRequest.getContent(), task))
                .zipWith(maxPositionMono, (checkItem, maxPosition) -> {
                    checkItem.setPosition(maxPosition + 1);
                    super.setCreatedBy(checkItem, exchange);
                    return checkItem;
                })
                .flatMap(checkItem -> checkItemRepository.save(checkItem))
                .map(CheckItemResponse::new)
                .doOnSuccess(checkItemResponse -> log.info("Check item {} has been added successfully", checkItemResponse.getId()))
                .doOnError(throwable -> log.error(throwable.getMessage()));
    }

    @Override
    public Mono<CheckItemResponse> update(String id, EditCheckItemRequest editCheckItemRequest) {
        return checkItemRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("check item with id " + id + " not found")))
                .flatMap(this::buildCheckItem)
                .map(currentCheckItem -> {
                    currentCheckItem.setChecked(editCheckItemRequest.isChecked());
                    return currentCheckItem;
                })
                .flatMap(currentCheckItem -> checkItemRepository.save(currentCheckItem))
                .map(CheckItemResponse::new)
                .doOnSuccess(checkItemResponse -> log.info("Check item with id '{}' saved successfully", checkItemResponse.getId()))
                .doOnError(throwable -> log.error(throwable.getMessage()));
    }

    @Override
    public Flux<CheckItemResponse> getAllByTaskId(String taskId, Pagination pagination) {
        Flux<CheckItem> checkItemFlux = taskRepository.findById(taskId)
                .switchIfEmpty(Mono.error(new NotFoundException( "Task not found")))
                .flatMapMany(task -> checkItemRepository.findAllByTask(task));

        return this.buildCheckItemResponseFlux(checkItemFlux, pagination);
    }

    @Override
    public Mono<Void> delete(String id) {
        return checkItemRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException( "Check item not found")))
                .flatMap(checkItem -> checkItemRepository.delete(checkItem))
                .doOnSuccess(voidValue -> log.info("Check item with id {} has been deleted successfully", id))
                .doOnError(throwable -> log.error(throwable.getMessage()));
    }

    @Override
    public Mono<Void> rearrange(List<RearrangedCheckItemRequest> rearrangedCheckItemRequests) {
        return Mono.justOrEmpty(rearrangedCheckItemRequests)
                .flatMapMany(Flux::fromIterable)
                .flatMap(request -> checkItemRepository.findById(request.getId())
                        .flatMap(this::buildCheckItem)
                        .map(checkItem -> {
                            checkItem.setPosition(request.getPosition());
                            return checkItem;
                        })
                )
                .flatMap(checkItem -> checkItemRepository.save(checkItem))
                .then();
    }

    private Mono<CheckItem> buildCheckItem(CheckItem checkItem) {
        Mono<List<Task>> taskListMono = taskRepository.findAll()
                .flatMap(super::buildTask)
                .collectList();

        return Mono.just(checkItem)
                .zipWith(taskListMono, (preparedCheckItem, tasks) -> {
                    Task parentTask = tasks.stream()
                            .filter(task -> task.getCheckItems().stream().anyMatch(childCheckItem -> preparedCheckItem.getId().equals(childCheckItem.getId())))
                            .findFirst()
                            .orElse(null);
                    preparedCheckItem.setTask(parentTask);
                    return preparedCheckItem;
                })
                .doOnError(throwable -> log.error(throwable.getMessage()));
    }

    private Flux<CheckItemResponse> buildCheckItemResponseFlux(Flux<CheckItem> checkItemFlux, Pagination pagination) {
        return super.paginate(checkItemFlux, pagination)
                .map(CheckItemResponse::new)
                .delayElements(Duration.ofMillis(100))
                .doOnError(throwable -> log.error(throwable.getMessage()));
    }
}
