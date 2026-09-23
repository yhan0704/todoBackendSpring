package backend.category.repository;

import backend.category.entity.Category;
import backend.category.entity.HiddenCategory;
import backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface HiddenCategoryRepository extends JpaRepository<HiddenCategory, Long> {
    boolean existsByUserAndCategory(User user, Category category);

    @Query("select hc.category.id from HiddenCategory hc where hc.user = :user")
    List<Long> findCategoryIdsByUser(User user);
}
