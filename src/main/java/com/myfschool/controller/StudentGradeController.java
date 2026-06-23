package com.myfschool.controller;

import com.myfschool.dto.response.ApiResponse;
import com.myfschool.entity.StudentGrade;
import com.myfschool.service.StudentGradeService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
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
}
