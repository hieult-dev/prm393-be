package com.myfschool.controller;

import com.myfschool.dto.response.ApiResponse;
import com.myfschool.dto.response.MarkDetailResponse;
import com.myfschool.dto.response.MarkReportSemesterResponse;
import com.myfschool.entity.StudentGrade;
import com.myfschool.service.StudentGradeService;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student-grades")
public class StudentGradeController extends AbstractCrudController<StudentGrade> {

    private final StudentGradeService service;

    public StudentGradeController(StudentGradeService service) {
        super(service);
        this.service = service;
    }

    @GetMapping("/search")
    public ApiResponse<List<StudentGrade>> search(@RequestParam Long userId, @RequestParam(required = false) Long semesterId) {
        if (semesterId == null) {
            return ApiResponse.success(service.findByUserId(userId));
        }
        return ApiResponse.success(service.findByUserIdAndSemesterId(userId, semesterId));
    }

    @GetMapping("/mark-report")
    public ApiResponse<List<MarkReportSemesterResponse>> markReport(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(service.getMarkReport(currentUserId(jwt)));
    }

    @GetMapping("/{id}/mark-detail")
    public ApiResponse<MarkDetailResponse> markDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(service.getMarkDetail(id, currentUserId(jwt), hasRole(jwt, "ADMIN")));
    }

    private Long currentUserId(Jwt jwt) {
        Object userId = jwt.getClaim("userId");
        if (userId instanceof Number number) {
            return number.longValue();
        }
        return Long.valueOf(userId.toString());
    }

    private boolean hasRole(Jwt jwt, String role) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        return roles != null && roles.contains(role);
    }
}
