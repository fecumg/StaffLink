package fpt.edu.user_service.entities;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "functions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Function extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotNull
    @Column(length = 50)
    private String name;

    @NotNull
    @Column(length = 500)
    private String description;

    @Nullable
    @Column(length = 500)
    private String uri;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Function parent;

    private boolean displayed = true;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private List<Function> children = new ArrayList<>();



    @Column(name = "created_by")
    private int createdBy;

    @Column(name = "updated_by")
    private int updatedBy;



    @ManyToMany(mappedBy = "functions", fetch = FetchType.LAZY)
    private List<Role> roles = new ArrayList<>();



    @OneToMany(mappedBy = "function", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<RoleFunctionMapping> roleFunctionMappings = new ArrayList<>();
}
