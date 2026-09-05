package backend.todo.service;

import backend.todo.dto.TodoCreateRequest;
import backend.todo.dto.response.TodoResponse;
import backend.todo.entity.Todo;
import backend.todo.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoService {

    private final TodoRepository todoRepository;

    public List<Todo> getAllTodos() {
        return todoRepository.findAll();
    }

    public Todo getTodo(Long id) {
        return todoRepository.findById(id).orElseThrow(() -> new RuntimeException("Todo not found: " + id));
    }

    @Transactional
    public TodoResponse createTodo(TodoCreateRequest request) {
        // record는 request.tasks(), request.done() 처럼 메서드 형식으로 값을 꺼냅니다.
        Todo todo = Todo.builder()
                .tasks(request.tasks())
                .done(request.done())
                .priority(request.priority())
                .dueDate(request.dueDate())
                .category(request.category())
                .build();
        Todo saved = todoRepository.save(todo);
        return TodoResponse.from(saved);
    }

    @Transactional
    public TodoResponse editTodo(Long id, TodoCreateRequest request) {
        Todo todo = todoRepository.findById(id).orElseThrow(() -> new RuntimeException("Todo not found"));
        todo.update(request.tasks(), request.done(), request.priority(), request.dueDate(), request.category());  // 수정!
        return TodoResponse.from(todo);
    }

    @Transactional
    public Long deleteTodo(Long id) {
        todoRepository.deleteById(id);
        return id;
    }
}