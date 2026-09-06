package backend.auth.dto.response;

import backend.user.dto.response.UserResponse;

public record LoginResponse(
        TokenResponse tokens,
        UserResponse user
) {}
