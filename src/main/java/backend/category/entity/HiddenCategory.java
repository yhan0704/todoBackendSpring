package backend.category.entity;

import backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "hidden_categories", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "category_id"}))
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HiddenCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
}
