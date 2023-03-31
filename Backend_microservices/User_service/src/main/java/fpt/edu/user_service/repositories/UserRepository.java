package fpt.edu.user_service.repositories;

import fpt.edu.user_service.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author Truong Duc Duong
 */

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);

    @Query(value = "select case when count(id) > 0 then true else false end from User where username = ?2 and id <> ?1")
    Boolean existsByEditedUsername(int id, String username);

    @Query(value = "select case when count(id) > 0 then true else false end from User where email = ?2 and id <> ?1")
    Boolean existsByEditedEmail(int id, String email);

    @Query(value = "select count (l.id) from LoginFailureLog l where l.user.username = ?1")
    int loginFailureCountByUsername(String username);

    @Query(value = "select urm.user from UserRoleMapping urm where urm.role.id = ?1")
    List<User> findAllByRoleId(int roleId);

    @Query(value = "select urm.user from UserRoleMapping urm inner join RoleFunctionMapping rfm on urm.role.id = rfm.role.id where rfm.function.id = ?1")
    List<User> findAllByFunctionId(int functionId);
}
