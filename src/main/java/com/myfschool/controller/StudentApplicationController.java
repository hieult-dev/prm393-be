package com.myfschool.controller;

import com.myfschool.dto.response.ApiResponse;
import com.myfschool.entity.StudentApplication;
import com.myfschool.service.StudentApplicationService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student-applications")
public class StudentApplicationController extends AbstractCrudController<StudentApplication> {

    private final StudentApplicationService service;

    public StudentApplicationController(StudentApplicationService service) {
        super(service);
        this.service = service;
    }

    @GetMapping("/search")
    public ApiResponse<List<StudentApplication>> search(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status
    ) {
        if (userId != null) {
            return ApiResponse.success(service.findByUserId(userId));
        }
        if (status != null && !status.isBlank()) {
            return ApiResponse.success(service.findByStatus(status));
        }
        return ApiResponse.success(service.findAll());
    }
}
