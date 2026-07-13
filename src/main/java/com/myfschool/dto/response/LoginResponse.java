package com.myfschool.dto.response;

import java.util.List;

public record LoginResponse(
        Long id,
        String userName,
        String firstName,
        String lastName,
        String email,
        String phone,
        String className,
        String role,
        List<String> roles,
        List<String> permissions,
        String status
) {
}
