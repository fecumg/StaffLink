package fpt.edu.user_service.pagination;

import org.springframework.data.domain.Sort;

import java.util.List;

/**
 * @author Truong Duc Duong
 */
public interface SortHandler<T> {
    List<T> handle(Sort sort);
}
