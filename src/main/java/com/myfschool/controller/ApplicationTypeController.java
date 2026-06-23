package com.myfschool.controller;

import com.myfschool.entity.ApplicationType;
import com.myfschool.service.ApplicationTypeService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/application-types")
public class ApplicationTypeController extends AbstractCrudController<ApplicationType> {

    public ApplicationTypeController(ApplicationTypeService service) {
        super(service);
    }
}
