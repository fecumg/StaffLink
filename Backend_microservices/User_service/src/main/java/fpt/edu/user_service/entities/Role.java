package fpt.edu.user_service.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Truong Duc Duong
 */

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Role extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotNull
    @Size(max = 50)
    private String name;

    @NotNull
    @Size(max = 500)
    private String description;

    @Column(name = "created_by")
    private int createdBy;

    @Column(name = "updated_by")
    private int updatedBy;



    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    private List<User> users = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "role_function_mappings",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "function_id")
    )
    private List<Function> functions = new ArrayList<>();



    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<UserRoleMapping> userRoleMapping = new ArrayList<>();



    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<RoleFunctionMapping> roleFunctionMappings = new ArrayList<>();
}
