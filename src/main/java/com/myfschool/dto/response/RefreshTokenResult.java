package com.myfschool.dto.response;

import com.myfschool.entity.User;

public record RefreshTokenResult(
        String token,
        long expiresIn,
        User user
) {
}
