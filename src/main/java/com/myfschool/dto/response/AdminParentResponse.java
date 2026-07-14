package com.myfschool.dto.response;

public record AdminParentResponse(
        Long id,
        String userName,
        String fullName,
        String email,
        String phone,
        String status
) {
}
