package com.myfschool.controller;

import com.myfschool.dto.request.AssignSubjectsRequest;
import com.myfschool.dto.request.AssignStudentsRequest;
import com.myfschool.dto.response.AdminGradeResponse;
import com.myfschool.dto.response.AdminParentResponse;
import com.myfschool.dto.response.AdminStudentResponse;
import com.myfschool.dto.response.AdminTeacherResponse;
import com.myfschool.dto.response.ApiResponse;
import com.myfschool.entity.Semester;
import com.myfschool.entity.Subject;
import com.myfschool.service.AdminGradeService;
import com.myfschool.service.ParentStudentService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminGradeController {

    private final AdminGradeService service;
    private final ParentStudentService parentStudentService;

    public AdminGradeController(AdminGradeService service, ParentStudentService parentStudentService) {
        this.service = service;
        this.parentStudentService = parentStudentService;
    }

    @GetMapping("/students")
    public ApiResponse<List<AdminStudentResponse>> students(
            @RequestParam(required = false) String search
    ) {
        return ApiResponse.success(service.getStudents(search));
    }

    @GetMapping("/teachers")
    public ApiResponse<List<AdminTeacherResponse>> teachers(
            @RequestParam(required = false) String search
    ) {
        return ApiResponse.success(service.getTeachers(search));
    }

    @GetMapping("/parents")
    public ApiResponse<List<AdminParentResponse>> parents(
            @RequestParam(required = false) String search
    ) {
        return ApiResponse.success(parentStudentService.getParents(search));
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
                "Gan mon hoc thanh cong",
                service.assignSubjects(userId, semesterId, request.subjectIds())
        );
    }

    @GetMapping("/teachers/{teacherId}/subjects")
    public ApiResponse<List<Subject>> teacherSubjects(
            @PathVariable Long teacherId
    ) {
        return ApiResponse.success(service.getTeacherSubjects(teacherId));
    }

    @PutMapping("/teachers/{teacherId}/subjects")
    public ApiResponse<List<Subject>> assignTeacherSubjects(
            @PathVariable Long teacherId,
            @Valid @RequestBody AssignSubjectsRequest request
    ) {
        return ApiResponse.success(
                "Gan mon cho teacher thanh cong",
                service.assignTeacherSubjects(teacherId, request.subjectIds())
        );
    }

    @GetMapping("/parents/{parentId}/students")
    public ApiResponse<List<AdminStudentResponse>> parentStudents(
            @PathVariable Long parentId
    ) {
        return ApiResponse.success(parentStudentService.getLinkedStudents(parentId));
    }

    @PutMapping("/parents/{parentId}/students")
    public ApiResponse<List<AdminStudentResponse>> assignParentStudents(
            @PathVariable Long parentId,
            @Valid @RequestBody AssignStudentsRequest request
    ) {
        return ApiResponse.success(
                "Gan sinh vien cho phu huynh thanh cong",
                parentStudentService.assignStudents(parentId, request.studentIds())
        );
    }

    @GetMapping("/grades")
    public ApiResponse<List<AdminGradeResponse>> grades(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long semesterId
    ) {
        return ApiResponse.success(service.getGrades(userId, semesterId));
    }
}
