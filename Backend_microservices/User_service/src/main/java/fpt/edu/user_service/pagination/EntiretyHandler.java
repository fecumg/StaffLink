package fpt.edu.user_service.pagination;

import java.util.List;

/**
 * @author Truong Duc Duong
 */
public interface EntiretyHandler<T> {
    List<T> handle();
}
