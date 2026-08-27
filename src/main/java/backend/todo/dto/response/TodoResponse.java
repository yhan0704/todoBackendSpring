package backend.todo.dto.response;

import backend.todo.entity.Todo;

public record TodoResponse(
        Long id,
        String tasks,
        boolean done,
        java.time.LocalDateTime createdAt,
        java.time.LocalDateTime updatedAt
    ) {
    public static TodoResponse from(Todo todo) {
        return new TodoResponse(
                todo.getId(),
                todo.getTasks(),
                todo.isDone(),
                todo.getCreatedAt(),
                todo.getUpdatedAt()
        );
    }
}