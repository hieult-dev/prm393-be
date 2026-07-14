package com.myfschool.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ReviewStudentApplicationRequest(
        @NotBlank
        String status,

        String responseNote
) {
}
