package fpt.edu.user_service.repositories;

import fpt.edu.user_service.entities.Function;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Truong Duc Duong
 */

@Repository
public interface FunctionRepository extends JpaRepository<Function, Integer> {

    boolean existsByUri(String uri);
    boolean existsByUriAndUriIsNot(String newUri, String currentUri);
}