package fpt.edu.user_service.pagination;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

/**
 * @author Truong Duc Duong
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pagination {
    public static final String ASC = "ASC";
    public static final String DESC = "DESC";
    
    private int pageNumber = 0;
    private int pageSize = 10;
    private String sortBy = "id";
    private String direction = ASC;

    public static PageRequest getPageRequest(Pagination pagination) {
        return PageRequest.of(
                pagination.getPageNumber() - 1,
                pagination.getPageSize(),
                Sort.by(Sort.Direction.valueOf(pagination.getDirection()), pagination.getSortBy())
        );
    }

    public static Sort getSort(Pagination pagination) {
        return Sort.by(Sort.Direction.valueOf(pagination.getDirection()), pagination.getSortBy());
    }

    public static boolean isPaginationValid(Pagination pagination) {
        return pagination != null && pagination.getPageNumber() > 0 && pagination.getPageSize() > 0;
    }

    public static <T> List<T> retrieve(Pagination pagination, EntiretyHandler<T> entiretyHandler, PaginationHandler<T> paginationHandler, SortHandler<T> sortHandler, Class<T> tClass) {
        if (pagination == null) {
            return entiretyHandler.handle();
        } else if (isPaginationValid(pagination)) {
            PageRequest pageRequest = getPageRequest(pagination);
            return paginationHandler.handle(pageRequest);
        } else {
            Sort sort = getSort(pagination);
            return sortHandler.handle(sort);
        }
    }
}
