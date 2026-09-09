package backend.user.entity;

import backend.todo.entity.Todo;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String password;
    private String refreshToken;

    private String nickname;
    private String phone;
    private LocalDate birthDate;

    @OneToMany(mappedBy = "user")
    private List<Todo> todos;

    public void updateEmail(String email) {
        this.email = email;
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}