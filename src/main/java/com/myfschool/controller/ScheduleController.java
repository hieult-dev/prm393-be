package com.myfschool.controller;

import com.myfschool.dto.response.ApiResponse;
import com.myfschool.entity.Schedule;
import com.myfschool.service.ScheduleService;
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
@RequestMapping("/api/schedules")
public class ScheduleController extends AbstractCrudController<Schedule> {

    private final ScheduleService service;

    public ScheduleController(ScheduleService service) {
        super(service);
        this.service = service;
    }

    @GetMapping("/search")
    public ApiResponse<List<Schedule>> search(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long semesterId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate studyDate,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long requestedUserId = userId == null ? currentUserId(jwt) : userId;
        if (!hasRole(jwt, "ADMIN") && !requestedUserId.equals(currentUserId(jwt))) {
            throw new AccessDeniedException("You cannot view another user's schedule");
        }
        if (semesterId != null && studyDate != null) {
            return ApiResponse.success(
                    service.findByUserIdAndSemesterIdAndStudyDate(requestedUserId, semesterId, studyDate)
            );
        }
        if (semesterId != null) {
            return ApiResponse.success(service.findByUserIdAndSemesterId(requestedUserId, semesterId));
        }
        if (studyDate == null) {
            return ApiResponse.success(service.findByUserId(requestedUserId));
        }
        return ApiResponse.success(service.findByUserIdAndStudyDate(requestedUserId, studyDate));
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
