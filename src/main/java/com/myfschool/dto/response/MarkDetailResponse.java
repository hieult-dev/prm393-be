package com.myfschool.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record MarkDetailResponse(
        Long id,
        Long subjectId,
        String subjectCode,
        String subjectName,
        String className,
        BigDecimal average,
        String letterGrade,
        boolean passed,
        List<MarkDetailItemResponse> items
) {
}
