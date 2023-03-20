package fpt.edu.user_service.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import fpt.edu.user_service.customAnnotations.Phone;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.Nullable;

import java.util.Date;
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
    @Size(max = 50)
    private String name;

    @NotNull
    @Column(unique = true)
    @Size(min = 6, max = 20)
    private String username;

    @NotNull
    @Size(max = 50)
    private String address;

    @Phone
    private String phone;

    @Email
    @Column(unique = true)
    private String email;

    @NotNull
    @Size(min = 6)
    private String password;

    @Nullable
    private String avatar;

    @JsonBackReference
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LoginFailureLog> loginFailureLogs;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;




    @JsonBackReference
    @OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY)
    private List<User> createdUsers;

    @JsonBackReference
    @OneToMany(mappedBy = "updatedBy", fetch = FetchType.LAZY)
    private List<User> updatedUsers;



    @JsonBackReference
    @OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY)
    private List<Role> createdRoles;

    @JsonBackReference
    @OneToMany(mappedBy = "updatedBy", fetch = FetchType.LAZY)
    private List<Role> updatedRoles;



    @JsonBackReference
    @OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY)
    private List<Function> createdFunctions;

    @JsonBackReference
    @OneToMany(mappedBy = "updatedBy", fetch = FetchType.LAZY)
    private List<Function> updatedFunctions;



    @JsonBackReference
    @OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY)
    private List<UserRoleMapping> createdUserRoleMappings;

    @JsonBackReference
    @OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY)
    private List<RoleFunctionMapping> createdRoleFunctionMappings;



    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_role_mappings",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<Role> roles;



    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserRoleMapping> userRoleMappings;


    public User(Date createdAt, Date updatedAt, int id, String name, String username, String address, String phone, String email, String password, @Nullable String avatar, List<LoginFailureLog> loginFailureLogs, User createdBy, User updatedBy, List<Role> roles) {
        super(createdAt, updatedAt);
        this.id = id;
        this.name = name;
        this.username = username;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.password = password;
        this.avatar = avatar;
        this.loginFailureLogs = loginFailureLogs;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.roles = roles;
    }
}
