package com.myfschool.dto.response;

import java.math.BigDecimal;

public record MarkReportGradeResponse(
        Long id,
        Long subjectId,
        String subjectCode,
        String subjectName,
        String className,
        BigDecimal average,
        String letterGrade,
        boolean passed
) {
}
