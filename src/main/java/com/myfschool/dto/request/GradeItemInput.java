package com.myfschool.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record GradeItemInput(
        @NotBlank
        @Size(max = 100)
        String name,

        @NotNull
        @DecimalMin("0.01")
        @DecimalMax("100.00")
        BigDecimal weight,

        @NotNull
        @DecimalMin("0.00")
        @DecimalMax("10.00")
        BigDecimal score
) {
}
