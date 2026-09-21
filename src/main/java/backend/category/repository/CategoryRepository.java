package backend.category.repository;

import backend.category.entity.Category;
import backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUserIsNullOrUser(User user);
}
