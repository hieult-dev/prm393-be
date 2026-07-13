package com.myfschool.dto.response;

import java.math.BigDecimal;

public record AdminGradeItemResponse(
        Long id,
        String name,
        BigDecimal weight,
        BigDecimal score
) {
}
