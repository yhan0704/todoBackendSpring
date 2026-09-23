package backend.todo.entity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import backend.category.entity.Category;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate  // 추가
    private LocalDateTime updatedAt;

    public void update(String tasks, boolean done, String priority, LocalDate dueDate, Category category) {
        this.tasks = tasks;
        this.done = done;
        this.priority = priority;
        this.dueDate = dueDate;
        this.category = category;
        this.updatedAt = LocalDateTime.now();
    }
}