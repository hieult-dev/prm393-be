package com.myfschool.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Username must not be blank")
        String userName,

        @JsonAlias("userPassword")
        @NotBlank(message = "Password must not be blank")
        @Size(min = 6, message = "Password must have at least 6 characters")
        String password
) {
}
