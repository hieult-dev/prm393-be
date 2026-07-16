package com.myfschool.controller;

import com.myfschool.dto.response.ApiResponse;
import com.myfschool.dto.response.AttendanceReportItemResponse;
import com.myfschool.dto.response.AttendanceReportSemesterResponse;
import com.myfschool.service.AttendanceReportService;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attendance-reports")
public class AttendanceReportController {

    private final AttendanceReportService service;

    public AttendanceReportController(AttendanceReportService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public ApiResponse<List<AttendanceReportSemesterResponse>> myAttendanceReport(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(service.getReport(currentUserId(jwt)));
    }

    @GetMapping("/search")
    public ApiResponse<List<AttendanceReportItemResponse>> search(
            @RequestParam Long userId,
            @RequestParam Long semesterId
    ) {
        return ApiResponse.success(service.getSemesterReport(userId, semesterId));
    }

    private Long currentUserId(Jwt jwt) {
        Object userId = jwt.getClaim("userId");
        if (userId instanceof Number number) {
            return number.longValue();
        }
        return Long.valueOf(userId.toString());
    }
}
