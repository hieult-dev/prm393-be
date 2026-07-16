package com.myfschool.dto.response;

import java.time.LocalDate;

public record AttendanceReportItemResponse(
        Long subjectId,
        String subjectCode,
        String subjectName,
        String className,
        LocalDate startDate,
        LocalDate endDate,
        long attendedSessions,
        long totalSessions,
        long absentSessions,
        int attendancePercentage
) {
}
