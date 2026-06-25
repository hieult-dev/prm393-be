package com.myfschool.dto.response;

import java.time.LocalDate;
import java.util.List;

public record MarkReportSemesterResponse(
        Long id,
        String name,
        String schoolYear,
        LocalDate startDate,
        LocalDate endDate,
        List<MarkReportGradeResponse> grades
) {
}
