package com.myfschool.controller;

import com.myfschool.dto.request.LoginRequest;
import com.myfschool.dto.request.LogoutRequest;
import com.myfschool.dto.request.RefreshTokenRequest;
import com.myfschool.dto.request.RegisterRequest;
import com.myfschool.dto.request.ResetPasswordRequest;
import com.myfschool.dto.response.ApiResponse;
import com.myfschool.dto.response.AuthResponse;
import com.myfschool.dto.response.ResetTokenResponse;
import com.myfschool.service.AuthenticationService;
import com.myfschool.service.ForgotPasswordService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final ForgotPasswordService forgotPasswordService;

    public AuthController(
            AuthenticationService authenticationService,
            ForgotPasswordService forgotPasswordService
    ) {
        this.authenticationService = authenticationService;
        this.forgotPasswordService = forgotPasswordService;
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success("Đăng nhập thành công", authenticationService.login(request));
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success("Refresh token thanh cong", authenticationService.refresh(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
        authenticationService.logout(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password/verify-phone")
    public ApiResponse<ResetTokenResponse> verifyForgotPasswordPhone(
            @RequestHeader(value = "X-Firebase-ID-Token", required = false) String firebaseIdToken
    ) {
        return ApiResponse.success(forgotPasswordService.verifyPhone(firebaseIdToken));
    }

    @PostMapping("/forgot-password/reset")
    public ApiResponse<Void> resetForgotPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        forgotPasswordService.resetPassword(request);
        return ApiResponse.success("Đổi mật khẩu thành công", null);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đăng ký thành công", authenticationService.register(request)));
    }
}
