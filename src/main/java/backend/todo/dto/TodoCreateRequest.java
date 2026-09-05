package backend.todo.dto;

import java.time.LocalDate;

public record TodoCreateRequest(
        String tasks,
        boolean done,
        String priority,
        LocalDate dueDate,
        String category
) {
}
