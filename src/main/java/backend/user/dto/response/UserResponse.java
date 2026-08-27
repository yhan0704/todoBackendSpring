package backend.user.dto.response;

import backend.user.entity.User;

public record UserResponse(
        Long id,
        String name,
        String email
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }
}