package com.myfschool.dto.response;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        LoginResponse user
) {
}
