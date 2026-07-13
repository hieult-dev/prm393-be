package com.myfschool.dto.response;

public record AdminStudentResponse(
        Long id,
        String userName,
        String fullName,
        String email,
        String className,
        String status
) {
}
