package com.myfschool.controller;

import com.myfschool.entity.Subject;
import com.myfschool.service.SubjectService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController extends AbstractCrudController<Subject> {

    public SubjectController(SubjectService service) {
        super(service);
    }
}
