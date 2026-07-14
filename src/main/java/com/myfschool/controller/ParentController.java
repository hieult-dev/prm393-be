package com.myfschool.controller;

import com.myfschool.dto.response.AdminStudentResponse;
import com.myfschool.dto.response.ApiResponse;
import com.myfschool.dto.response.MarkDetailResponse;
import com.myfschool.dto.response.MarkReportSemesterResponse;
import com.myfschool.dto.response.ScheduleItemResponse;
import com.myfschool.service.ParentStudentService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/parent")
public class ParentController {

    private final ParentStudentService service;

    public ParentController(ParentStudentService service) {
        this.service = service;
    }

    @GetMapping("/students")
    public ApiResponse<List<AdminStudentResponse>> students(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(service.getLinkedStudents(currentUserId(jwt)));
    }

    @GetMapping("/students/{studentId}/mark-report")
    public ApiResponse<List<MarkReportSemesterResponse>> markReport(
            @PathVariable Long studentId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(service.getChildMarkReport(currentUserId(jwt), studentId));
    }

    @GetMapping("/students/{studentId}/grades/{gradeId}/mark-detail")
    public ApiResponse<MarkDetailResponse> markDetail(
            @PathVariable Long studentId,
            @PathVariable Long gradeId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(service.getChildMarkDetail(currentUserId(jwt), studentId, gradeId));
    }

    @GetMapping("/students/{studentId}/schedules/day")
    public ApiResponse<List<ScheduleItemResponse>> daySchedule(
            @PathVariable Long studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate studyDate,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(service.getChildDailySchedule(currentUserId(jwt), studentId, studyDate));
    }

    @GetMapping("/students/{studentId}/schedules/weekly")
    public ApiResponse<List<ScheduleItemResponse>> weeklySchedule(
            @PathVariable Long studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(service.getChildWeeklySchedule(currentUserId(jwt), studentId, weekStart));
    }

    private Long currentUserId(Jwt jwt) {
        Object userId = jwt.getClaim("userId");
        if (userId instanceof Number number) {
            return number.longValue();
        }
        return Long.valueOf(userId.toString());
    }
}
