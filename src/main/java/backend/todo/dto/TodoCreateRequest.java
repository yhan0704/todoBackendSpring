package backend.todo.dto;

public record TodoCreateRequest(
        String tasks,
        boolean done
) {
}