package backend.todo.service;

import backend.category.entity.Category;
import backend.category.repository.CategoryRepository;
import backend.todo.dto.TodoCreateRequest;
import backend.todo.dto.response.TodoResponse;
import backend.todo.entity.Todo;
import backend.todo.repository.TodoRepository;
import backend.user.entity.User;
import backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoService {

    private final TodoRepository todoRepository;
    private final CategoryRepository categoryRepository;
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
                .category(resolveCategory(request.categoryId(), user))
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
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        Todo todo = todoRepository.findById(id).orElseThrow(() -> new RuntimeException("Todo not found"));
        // id 비교로 소유자 확인 — todo.getUser()와 currentUser는 JPA가 따로 조회한 별개의 인스턴스라
        // 기본 equals()(참조 비교)로는 같은 유저여도 false가 나옴
        if (!todo.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("본인 소유의 Todo만 수정할 수 있습니다");
        }
        todo.update(
                request.tasks(), request.done(), request.priority(), request.dueDate(),
                resolveCategory(request.categoryId(), currentUser)
        );  // 수정!
        return TodoResponse.from(todo);
    }

    // 카테고리는 추천(user null) 또는 본인 소유만 todo에 붙일 수 있음 — GET /categories에서
    // 보여주는 범위와 동일한 규칙.
    private Category resolveCategory(Long categoryId, User currentUser) {
        if (categoryId == null) return null;
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found: " + categoryId));
        if (category.getUser() != null && !category.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("본인 소유이거나 추천 카테고리만 사용할 수 있습니다");
        }
        return category;
    }

    @Transactional
    public Long deleteTodo(Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        Todo todo = todoRepository.findById(id).orElseThrow(() -> new RuntimeException("Todo not found: " + id));
        // editTodo와 동일한 소유자 확인 — 없으면 다른 유저의 todo도 지울 수 있음
        if (!todo.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("본인 소유의 Todo만 삭제할 수 있습니다");
        }
        todoRepository.deleteById(id);
        return id;
    }
}