package com.myfschool.dto.response;

import java.math.BigDecimal;

public record ProfileAcademicSummaryResponse(
        Long semesterId,
        String semesterName,
        String schoolYear,
        BigDecimal gpa,
        int gradedSubjects,
        int totalCredits
) {
}
