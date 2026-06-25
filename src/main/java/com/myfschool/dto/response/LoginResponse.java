package com.myfschool.dto.response;

public record LoginResponse(
        Long id,
        String studentCode,
        String fullName,
        String email,
        String phone,
        String className,
        String role,
        String status
) {
}
