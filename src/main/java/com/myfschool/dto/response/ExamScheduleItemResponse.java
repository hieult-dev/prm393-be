package com.myfschool.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;

public record ExamScheduleItemResponse(
        Long id,
        Long semesterId,
        String semesterName,
        Long subjectId,
        String subjectCode,
        String subjectName,
        String examType,
        LocalDate examDate,
        LocalTime startTime,
        LocalTime endTime,
        String room,
        String seatNumber,
        String proctorName,
        String note,
        String status
) {
}
