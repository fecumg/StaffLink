package fpt.edu.taskservice.pagination;

import jakarta.validation.constraints.Min;
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

    @Min(value = 1, message = "Page number must be greater than 0")
    private int pageNumber = 1;

    @Min(value = 0, message = "Page size cannot not be negative")
    private int pageSize = 10;
    private String sortBy = "id";

    private String direction = ASC;
}
