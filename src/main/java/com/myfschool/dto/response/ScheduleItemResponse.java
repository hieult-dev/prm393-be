package com.myfschool.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;

public record ScheduleItemResponse(
        Long id,
        Long semesterId,
        String semesterName,
        Long subjectId,
        String subjectCode,
        String subjectName,
        LocalDate studyDate,
        LocalTime startTime,
        LocalTime endTime,
        String room,
        String lecturerName,
        String note
) {
}