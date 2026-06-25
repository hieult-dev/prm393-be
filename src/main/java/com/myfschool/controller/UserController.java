package com.myfschool.controller;

import com.myfschool.dto.request.LoginRequest;
import com.myfschool.dto.response.ApiResponse;
import com.myfschool.dto.response.LoginResponse;
import com.myfschool.entity.User;
import com.myfschool.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController extends AbstractCrudController<User> {

    private final UserService userService;

    public UserController(UserService service) {
        super(service);
        this.userService = service;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = userService.login(loginRequest);
        return ApiResponse.success("Đăng nhập thành công", loginResponse);
    }
}
