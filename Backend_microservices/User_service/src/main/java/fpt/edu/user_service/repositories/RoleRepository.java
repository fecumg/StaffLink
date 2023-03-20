package fpt.edu.user_service.repositories;

import fpt.edu.user_service.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Truong Duc Duong
 */

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
}
