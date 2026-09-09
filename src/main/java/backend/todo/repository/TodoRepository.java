package backend.todo.repository;

import backend.todo.entity.Todo;
import backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TodoRepository extends JpaRepository<Todo, Long>{
    List<Todo> findByUser(User user);
}
