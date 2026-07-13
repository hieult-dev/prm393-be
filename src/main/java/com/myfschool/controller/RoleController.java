package com.myfschool.controller;

import com.myfschool.entity.Role;
import com.myfschool.service.RoleService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/roles")
public class RoleController extends AbstractCrudController<Role> {

    public RoleController(RoleService service) {
        super(service);
    }
}
