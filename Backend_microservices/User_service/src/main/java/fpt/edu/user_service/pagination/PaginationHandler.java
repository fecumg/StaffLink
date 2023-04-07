package fpt.edu.user_service.pagination;

import org.springframework.data.domain.PageRequest;

import java.util.List;

/**
 * @author Truong Duc Duong
 */
public interface PaginationHandler<T> {
    List<T> handle(PageRequest pageRequest);
}
