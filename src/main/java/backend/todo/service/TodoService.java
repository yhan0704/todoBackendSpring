package backend.todo.service;

import backend.todo.dto.TodoCreateRequest;
import backend.todo.dto.response.TodoResponse;
import backend.todo.entity.Todo;
import backend.todo.repository.TodoRepository;
import backend.user.entity.User;
import backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoService {

    private final TodoRepository todoRepository;
    private final UserRepository userRepository;

    public List<TodoResponse> getAllTodos() {
        return todoRepository.findAll().stream()
                .map(TodoResponse::from)
                .toList();
    }

    // 로그인한 유저 소유의 todos만 반환 — getAllTodos()는 전체 유저의 todos를 다 반환하므로
    // (나중에 admin 전용으로 쓸 수 있게 남겨둠) 컨트롤러에는 이 메서드만 노출함
    public List<TodoResponse> getMyTodos() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        return todoRepository.findByUser(user).stream()
                .map(TodoResponse::from)
                .toList();
    }

    public TodoResponse getTodo(Long id) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Todo not found: " + id));
        return TodoResponse.from(todo);
    }

    @Transactional
    public TodoResponse createTodo(TodoCreateRequest request) {
        // JwtAuthenticationFilter가 인증 성공 시 email을 principal로 SecurityContext에 넣어둠
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        // record는 request.tasks(), request.done() 처럼 메서드 형식으로 값을 꺼냅니다.
        Todo todo = Todo.builder()
                .tasks(request.tasks())
                .done(request.done())
                .priority(request.priority())
                .dueDate(request.dueDate())
                .category(request.category())
                // Todo에 소유자(user)를 연결 — 이게 빠지면 user_id가 null로 저장됨(오늘 실제로 겪은 버그).
                // Todo.user는 @ManyToOne(fetch=LAZY)로 매핑돼있어서, 여기서 세팅한 User 객체 참조가
                // 저장 시점에 Hibernate가 알아서 user_id 외래키 값으로 변환해서 넣어줌.
                .user(user)
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