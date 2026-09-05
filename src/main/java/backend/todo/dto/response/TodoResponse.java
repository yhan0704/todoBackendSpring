package backend.todo.dto.response;

import backend.todo.entity.Todo;

import java.time.LocalDate;

public record TodoResponse(
        Long id,
        String tasks,
        boolean done,
        String priority,
        LocalDate dueDate,
        String category,
        java.time.LocalDateTime createdAt,
        java.time.LocalDateTime updatedAt
    ) {
    public static TodoResponse from(Todo todo) {
        return new TodoResponse(
                todo.getId(),
                todo.getTasks(),
                todo.isDone(),
                todo.getPriority(),
                todo.getDueDate(),
                todo.getCategory(),
                todo.getCreatedAt(),
                todo.getUpdatedAt()
        );
    }
}
