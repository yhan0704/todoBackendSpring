package backend.category.entity;

import backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // null이면 모든 유저가 같이 쓰는 추천 카테고리, 값이 있으면 그 유저 전용 카테고리.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public void update(String name) {
        this.name = name;
    }
}
