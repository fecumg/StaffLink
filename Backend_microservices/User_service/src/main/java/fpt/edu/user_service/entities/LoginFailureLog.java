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
@Table(name = "login_failure_logs")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginFailureLog extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private boolean isHandled = false;

    public LoginFailureLog(User user) {
        this.user = user;
    }
}
