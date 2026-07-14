package com.myfschool.dto.response;

public record ResetTokenResponse(
        String resetToken,
        long expiresInSeconds
) {
}
