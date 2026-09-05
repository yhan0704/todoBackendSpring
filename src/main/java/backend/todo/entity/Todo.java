package backend.todo.entity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import backend.user.entity.User;
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
    private String priority;
    private LocalDate dueDate;
    private String category;

    // 지금은 nullable=true — TodoService.createTodo가 아직 로그인한 유저를 넣어주지 않아서
    // false로 하면 API로 만드는 todo마다 저장 실패남. join 연습용 seed 데이터만 우선 채움.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate  // 추가
    private LocalDateTime updatedAt;

    public void update(String tasks, boolean done, String priority, LocalDate dueDate, String category) {
        this.tasks = tasks;
        this.done = done;
        this.priority = priority;
        this.dueDate = dueDate;
        this.category = category;
        this.updatedAt = LocalDateTime.now();
    }
}