package fpt.edu.user_service.pagination;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * @author Truong Duc Duong
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pagination {
    public static final String ASC = "ASC";
    public static final String DESC = "DESC";
    
    private int pageNumber = 1;
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
}
