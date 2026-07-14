package com.myfschool.controller;

import com.myfschool.dto.request.SaveGradeRequest;
import com.myfschool.dto.response.AdminGradeResponse;
import com.myfschool.dto.response.AdminStudentResponse;
import com.myfschool.dto.response.ApiResponse;
import com.myfschool.dto.response.TeacherScheduleItemResponse;
import com.myfschool.entity.Semester;
import com.myfschool.entity.Subject;
import com.myfschool.service.AdminGradeService;
import com.myfschool.service.ScheduleService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teacher")
public class TeacherGradeController {

    private final AdminGradeService service;
    private final ScheduleService scheduleService;

    public TeacherGradeController(AdminGradeService service, ScheduleService scheduleService) {
        this.service = service;
        this.scheduleService = scheduleService;
    }

    @GetMapping("/subjects")
    public ApiResponse<List<Subject>> subjects(
            @RequestParam(required = false) Long semesterId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(service.getTeacherSubjects(currentUserId(jwt), semesterId));
    }

    @GetMapping("/semesters")
    public ApiResponse<List<Semester>> semesters() {
        return ApiResponse.success(service.getSemesters());
    }

    @GetMapping("/schedules")
    public ApiResponse<List<TeacherScheduleItemResponse>> schedules(
            @RequestParam(required = false) Long semesterId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate studyDate,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(scheduleService.findTeacherSchedule(
                currentUserId(jwt),
                semesterId,
                studyDate
        ));
    }

    @GetMapping("/schedules/day")
    public ApiResponse<List<TeacherScheduleItemResponse>> daySchedule(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate studyDate,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(scheduleService.findTeacherDailySchedule(currentUserId(jwt), studyDate));
    }

    @GetMapping("/schedules/weekly")
    public ApiResponse<List<TeacherScheduleItemResponse>> weeklySchedule(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(scheduleService.findTeacherWeeklySchedule(currentUserId(jwt), weekStart));
    }

    @GetMapping("/students")
    public ApiResponse<List<AdminStudentResponse>> students(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long semesterId,
            @RequestParam(required = false) String search,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(service.getTeacherStudents(
                currentUserId(jwt),
                subjectId,
                semesterId,
                search
        ));
    }

    @GetMapping("/grades")
    public ApiResponse<List<AdminGradeResponse>> grades(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long semesterId,
            @RequestParam(required = false) Long subjectId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(service.getTeacherGrades(
                currentUserId(jwt),
                userId,
                semesterId,
                subjectId
        ));
    }

    @PostMapping("/grades")
    public ResponseEntity<ApiResponse<AdminGradeResponse>> create(
            @Valid @RequestBody SaveGradeRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Nhap diem thanh cong",
                        service.createGradeForTeacher(currentUserId(jwt), request)
                ));
    }

    @PutMapping("/grades/{id}")
    public ApiResponse<AdminGradeResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody SaveGradeRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(
                "Cap nhat diem thanh cong",
                service.updateGradeForTeacher(currentUserId(jwt), id, request)
        );
    }

    @DeleteMapping("/grades/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        service.deleteGradeForTeacher(currentUserId(jwt), id);
        return ResponseEntity.noContent().build();
    }

    private Long currentUserId(Jwt jwt) {
        Object userId = jwt.getClaim("userId");
        if (userId instanceof Number number) {
            return number.longValue();
        }
        return Long.valueOf(userId.toString());
    }
}
