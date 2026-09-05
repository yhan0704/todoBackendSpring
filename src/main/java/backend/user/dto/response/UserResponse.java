package backend.user.dto.response;

import backend.user.entity.User;

import java.time.LocalDate;

public record UserResponse(
        Long id,
        String name,
        String email,
        String nickname,
        String phone,
        LocalDate birthDate
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getNickname(),
                user.getPhone(),
                user.getBirthDate()
        );
    }
}
