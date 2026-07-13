package com.myfschool.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record AdminGradeResponse(
        Long id,
        Long userId,
        String studentCode,
        String studentName,
        String className,
        Long subjectId,
        String subjectCode,
        String subjectName,
        Long semesterId,
        String semesterName,
        BigDecimal totalScore,
        String letterGrade,
        List<AdminGradeItemResponse> items
) {
}
