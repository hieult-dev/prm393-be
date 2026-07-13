package com.myfschool.controller;

import com.myfschool.dto.response.ApiResponse;
import com.myfschool.entity.SemesterSubject;
import com.myfschool.service.SemesterSubjectService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/semester-subjects")
public class SemesterSubjectController extends AbstractCrudController<SemesterSubject> {

    private final SemesterSubjectService service;

    public SemesterSubjectController(SemesterSubjectService service) {
        super(service);
        this.service = service;
    }

    @GetMapping("/search")
    public ApiResponse<List<SemesterSubject>> search(
            @RequestParam(required = false) Long semesterId,
            @RequestParam(required = false) Long subjectId
    ) {
        if (semesterId != null && subjectId != null) {
            return ApiResponse.success(service.findBySemesterIdAndSubjectId(semesterId, subjectId));
        }
        if (semesterId != null) {
            return ApiResponse.success(service.findBySemesterId(semesterId));
        }
        if (subjectId != null) {
            return ApiResponse.success(service.findBySubjectId(subjectId));
        }
        return ApiResponse.success(service.findAll());
    }
}
