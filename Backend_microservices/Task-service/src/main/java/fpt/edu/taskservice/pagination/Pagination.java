package fpt.edu.taskservice.pagination;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    public static boolean isPaginationValid(Pagination pagination) {
        return pagination != null && pagination.getPageNumber() > 0 && pagination.getPageSize() > 0;
    }
}
