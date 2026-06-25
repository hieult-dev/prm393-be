package com.myfschool.dto.response;

import java.math.BigDecimal;

public record MarkDetailItemResponse(
        Long id,
        String gradeCategory,
        String gradeItem,
        BigDecimal weight,
        String value
) {
}
