package fpt.edu.user_service.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Truong Duc Duong
 */

@Entity
@Table(name = "user_role_mappings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;

    public UserRoleMapping(User user, Role role) {
        this.user = user;
        this.role = role;
    }
}
