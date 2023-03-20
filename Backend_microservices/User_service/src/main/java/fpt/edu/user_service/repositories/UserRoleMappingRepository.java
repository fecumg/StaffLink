package fpt.edu.user_service.repositories;

import fpt.edu.user_service.entities.UserRoleMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Truong Duc Duong
 */

@Repository
public interface UserRoleMappingRepository extends JpaRepository<UserRoleMapping, Integer> {
}
