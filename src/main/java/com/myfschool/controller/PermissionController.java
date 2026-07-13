package com.myfschool.controller;

import com.myfschool.entity.Permission;
import com.myfschool.service.PermissionService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController extends AbstractCrudController<Permission> {

    public PermissionController(PermissionService service) {
        super(service);
    }
}
