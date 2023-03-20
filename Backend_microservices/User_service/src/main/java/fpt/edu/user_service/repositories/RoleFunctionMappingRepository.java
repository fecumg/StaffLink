package fpt.edu.user_service.repositories;

import fpt.edu.user_service.entities.RoleFunctionMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Truong Duc Duong
 */

@Repository
public interface RoleFunctionMappingRepository extends JpaRepository<RoleFunctionMapping, Integer> {
}
