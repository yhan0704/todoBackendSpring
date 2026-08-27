package backend.todo.controller;

import backend.todo.dto.TodoCreateRequest;
import backend.todo.dto.response.TodoResponse;
import backend.todo.entity.Todo;
import backend.todo.service.TodoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/todos")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    @GetMapping
    public ResponseEntity<List<Todo>> getAllTodos() {
        return ResponseEntity.ok(todoService.getAllTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Todo> getTodo(@PathVariable Long id) {
        return ResponseEntity.ok(todoService.getTodo(id));
    }

    @PostMapping
    public ResponseEntity<TodoResponse> createTodo(@RequestBody TodoCreateRequest request) {
        return ResponseEntity.ok(todoService.createTodo(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TodoResponse> editTodo(
            @PathVariable Long id,
            @RequestBody TodoCreateRequest request) {
        return ResponseEntity.ok(todoService.editTodo(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Long> deleteTodo(@PathVariable Long id) {
        return ResponseEntity.ok(todoService.deleteTodo(id));
    }
}