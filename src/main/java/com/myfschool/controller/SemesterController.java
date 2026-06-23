package com.myfschool.controller;

import com.myfschool.entity.Semester;
import com.myfschool.service.SemesterService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/semesters")
public class SemesterController extends AbstractCrudController<Semester> {

    public SemesterController(SemesterService service) {
        super(service);
    }
}
