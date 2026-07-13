package com.myfschool.controller;

import com.myfschool.dto.request.AssignSubjectsRequest;
import com.myfschool.dto.request.SaveGradeRequest;
import com.myfschool.dto.response.AdminGradeResponse;
import com.myfschool.dto.response.AdminStudentResponse;
import com.myfschool.dto.response.ApiResponse;
import com.myfschool.entity.Semester;
import com.myfschool.entity.Subject;
import com.myfschool.service.AdminGradeService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/admin")
public class AdminGradeController {

    private final AdminGradeService service;

    public AdminGradeController(AdminGradeService service) {
        this.service = service;
    }

    @GetMapping("/students")
    public ApiResponse<List<AdminStudentResponse>> students(
            @RequestParam(required = false) String search
    ) {
        return ApiResponse.success(service.getStudents(search));
    }

    @GetMapping("/subjects")
    public ApiResponse<List<Subject>> subjects(
            @RequestParam(required = false) Long semesterId
    ) {
        return ApiResponse.success(service.getSubjects(semesterId));
    }

    @GetMapping("/semesters")
    public ApiResponse<List<Semester>> semesters() {
        return ApiResponse.success(service.getSemesters());
    }

    @GetMapping("/students/{userId}/semesters/{semesterId}/subjects")
    public ApiResponse<List<Subject>> assignedSubjects(
            @PathVariable Long userId,
            @PathVariable Long semesterId
    ) {
        return ApiResponse.success(service.getAssignedSubjects(userId, semesterId));
    }

    @PutMapping("/students/{userId}/semesters/{semesterId}/subjects")
    public ApiResponse<List<Subject>> assignSubjects(
            @PathVariable Long userId,
            @PathVariable Long semesterId,
            @Valid @RequestBody AssignSubjectsRequest request
    ) {
        return ApiResponse.success(
                "Gán môn học thành công",
                service.assignSubjects(userId, semesterId, request.subjectIds())
        );
    }

    @GetMapping("/grades")
    public ApiResponse<List<AdminGradeResponse>> grades(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long semesterId
    ) {
        return ApiResponse.success(service.getGrades(userId, semesterId));
    }

    @PostMapping("/grades")
    public ResponseEntity<ApiResponse<AdminGradeResponse>> create(
            @Valid @RequestBody SaveGradeRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Nhập điểm thành công", service.createGrade(request)));
    }

    @PutMapping("/grades/{id}")
    public ApiResponse<AdminGradeResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody SaveGradeRequest request
    ) {
        return ApiResponse.success("Cập nhật điểm thành công", service.updateGrade(id, request));
    }

    @DeleteMapping("/grades/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteGrade(id);
        return ResponseEntity.noContent().build();
    }
}
