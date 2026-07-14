package com.myfschool.dto.response;

public record AdminTeacherResponse(
        Long id,
        String userName,
        String fullName,
        String email,
        String phone,
        String status
) {
}
