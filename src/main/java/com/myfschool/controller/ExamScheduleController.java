package com.myfschool.controller;

import com.myfschool.dto.response.ApiResponse;
import com.myfschool.dto.response.ExamScheduleItemResponse;
import com.myfschool.entity.ExamSchedule;
import com.myfschool.service.ExamScheduleService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exam-schedules")
public class ExamScheduleController extends AbstractCrudController<ExamSchedule> {

    private final ExamScheduleService service;

    public ExamScheduleController(ExamScheduleService service) {
        super(service);
        this.service = service;
    }

    @GetMapping("/me")
    public ApiResponse<List<ExamScheduleItemResponse>> myExamSchedule(
            @RequestParam(required = false) Long semesterId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(service.findForStudent(currentUserId(jwt), semesterId));
    }

    @GetMapping("/search")
    public ApiResponse<List<ExamSchedule>> search(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long semesterId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate examDate,
            @RequestParam(required = false) String status,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long requestedUserId = userId;
        String requestedStatus = status;
        if (!hasRole(jwt, "ADMIN")) {
            Long currentUserId = currentUserId(jwt);
            if (requestedUserId == null) {
                requestedUserId = currentUserId;
            } else if (!requestedUserId.equals(currentUserId)) {
                throw new AccessDeniedException("You cannot view another user's exam schedule");
            }
            requestedStatus = "PUBLISHED";
        }
        return ApiResponse.success(service.search(requestedUserId, semesterId, examDate, requestedStatus));
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
