package com.myfschool.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank
        @Size(max = 250)
        String userName,

        @NotBlank
        @Size(min = 6, max = 72)
        String password,

        @NotBlank
        @Size(max = 50)
        String firstName,

        @Size(max = 50)
        String lastName,

        @NotBlank
        @Email
        @Size(max = 50)
        String email,

        @Size(max = 20)
        String phone,

        @Size(max = 50)
        String className
) {
}
