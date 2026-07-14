package com.myfschool.controller;

import com.myfschool.dto.response.ApiResponse;
import com.myfschool.dto.response.LoginResponse;
import com.myfschool.dto.response.ProfileAcademicSummaryResponse;
import com.myfschool.service.StudentGradeService;
import com.myfschool.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserService userService;
    private final StudentGradeService studentGradeService;

    public ProfileController(UserService userService, StudentGradeService studentGradeService) {
        this.userService = userService;
        this.studentGradeService = studentGradeService;
    }

    @GetMapping
    public ApiResponse<LoginResponse> currentProfile(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(userService.getCurrentProfile(currentUserId(jwt)));
    }

    @GetMapping("/academic-summary")
    public ApiResponse<ProfileAcademicSummaryResponse> academicSummary(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(
                studentGradeService.getCurrentAcademicSummary(currentUserId(jwt))
        );
    }

    private Long currentUserId(Jwt jwt) {
        Object userId = jwt.getClaim("userId");
        if (userId instanceof Number number) {
            return number.longValue();
        }
        return Long.valueOf(userId.toString());
    }
}
