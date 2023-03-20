package fpt.edu.user_service.repositories;

import fpt.edu.user_service.entities.LoginFailureLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Truong Duc Duong
 */

@Repository
public interface LoginFailureLogRepository extends JpaRepository<LoginFailureLog, Integer> {
}
