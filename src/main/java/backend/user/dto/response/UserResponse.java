package backend.user.dto.response;

import backend.todo.dto.response.TodoResponse;
import backend.user.entity.User;

import java.time.LocalDate;
import java.util.List;

public record UserResponse(
        Long id,
        String name,
        String email,
        String nickname,
        String phone,
        LocalDate birthDate,
        List<TodoResponse> todos
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getNickname(),
                user.getPhone(),
                user.getBirthDate(),
                user.getTodos() == null
                        ? List.of()
                        : user.getTodos().stream().map(TodoResponse::from).toList()
        );
    }
}
