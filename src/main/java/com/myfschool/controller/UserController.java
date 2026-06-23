package com.myfschool.controller;

import com.myfschool.entity.User;
import com.myfschool.service.UserService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController extends AbstractCrudController<User> {

    public UserController(UserService service) {
        super(service);
    }
}
