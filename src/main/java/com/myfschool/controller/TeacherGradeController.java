package com.myfschool.controller;

import com.myfschool.dto.request.ReviewStudentApplicationRequest;
import com.myfschool.dto.request.SaveGradeRequest;
import com.myfschool.dto.response.AdminGradeResponse;
import com.myfschool.dto.response.AdminStudentResponse;
import com.myfschool.dto.response.ApiResponse;
import com.myfschool.dto.response.GradeImportResultResponse;
import com.myfschool.dto.response.TeacherScheduleItemResponse;
import com.myfschool.entity.Semester;
import com.myfschool.entity.StudentApplication;
import com.myfschool.entity.Subject;
import com.myfschool.service.AdminGradeService;
import com.myfschool.service.ScheduleService;
import com.myfschool.service.StudentApplicationService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/teacher")
public class TeacherGradeController {

    private final AdminGradeService service;
    private final ScheduleService scheduleService;
    private final StudentApplicationService studentApplicationService;

    public TeacherGradeController(
            AdminGradeService service,
            ScheduleService scheduleService,
            StudentApplicationService studentApplicationService
    ) {
        this.service = service;
        this.scheduleService = scheduleService;
        this.studentApplicationService = studentApplicationService;
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


    @GetMapping("/applications")
    public ApiResponse<List<StudentApplication>> applications(
            @RequestParam(required = false) String status,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(studentApplicationService.findForHomeroomTeacher(
                currentUserId(jwt),
                status
        ));
    }

    @GetMapping("/applications/search")
    public ApiResponse<List<StudentApplication>> searchApplications(
            @RequestParam(required = false) String status,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return applications(status, jwt);
    }

    @PatchMapping("/applications/{id}/review")
    public ApiResponse<StudentApplication> reviewApplication(
            @PathVariable Long id,
            @Valid @RequestBody ReviewStudentApplicationRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(
                "Cập nhật trạng thái đơn thành công",
                studentApplicationService.reviewForHomeroomTeacher(id, request, currentUserId(jwt))
        );
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

    @GetMapping("/grades/template")
    public ResponseEntity<byte[]> gradeTemplate(
            @RequestParam Long semesterId,
            @RequestParam Long subjectId,
            @RequestParam(required = false) String className,
            @AuthenticationPrincipal Jwt jwt
    ) {
        byte[] file = service.buildTeacherGradeTemplate(
                currentUserId(jwt),
                semesterId,
                subjectId,
                className
        );
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"grade-template.xlsx\""
                )
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                ))
                .body(file);
    }

    @PostMapping(value = "/grades/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<GradeImportResultResponse> importGrades(
            @RequestParam Long semesterId,
            @RequestParam Long subjectId,
            @RequestParam(required = false) String className,
            @RequestParam MultipartFile file,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(
                "Import điểm thành công",
                service.importTeacherGrades(
                        currentUserId(jwt),
                        semesterId,
                        subjectId,
                        className,
                        file
                )
        );
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
