package fpt.edu.user_service.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import fpt.edu.user_service.customAnnotations.Phone;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Truong Duc Duong
 */

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotNull
    @Column(length = 50)
    private String name;

    @NotNull
    @Column(length = 20, unique = true)
    private String username;

    @NotNull
    @Column(length = 50)
    private String address;

    @Phone
    private String phone;

    @Email
    @Column(unique = true)
    private String email;

    @NotNull
    @Column(length = 500)
    private String password;

    @Nullable
    private String avatar;

    @JsonBackReference
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<LoginFailureLog> loginFailureLogs = new ArrayList<>();

    @Column(name = "created_by")
    private int createdBy;

    @Column(name = "updated_by")
    private int updatedBy;



    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_role_mappings",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<Role> roles = new ArrayList<>();



    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<UserRoleMapping> userRoleMappings = new ArrayList<>();
}


