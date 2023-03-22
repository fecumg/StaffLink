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
@Table(name = "role_function_mappings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleFunctionMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Role role;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "function_id")
    private Function function;

    public RoleFunctionMapping(Role role, Function function) {
        this.role = role;
        this.function = function;
    }
}
