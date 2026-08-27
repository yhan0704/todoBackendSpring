package backend.todo.entity;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "todos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)  //날짜 자동입력시키고 싶을때
public class Todo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String tasks;
    private boolean done;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate  // 추가
    private LocalDateTime updatedAt;

    public void update(String tasks, boolean done) {
        this.tasks = tasks;
        this.done = done;
        this.updatedAt = LocalDateTime.now();
    }
}