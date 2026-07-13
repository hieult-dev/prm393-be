package com.myfschool.controller;

import com.myfschool.dto.response.ApiResponse;
import com.myfschool.entity.StudentApplication;
import com.myfschool.service.StudentApplicationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @Override
    @GetMapping
    public ApiResponse<List<StudentApplication>> findAll() {
        Jwt jwt = currentJwt();
        if (hasRole(jwt, "ADMIN")) {
            return ApiResponse.success(service.findAll());
        }
        return ApiResponse.success(service.findByUserId(currentUserId(jwt)));
    }

    @Override
    @GetMapping("/{id}")
    public ApiResponse<StudentApplication> findById(@PathVariable Long id) {
        Jwt jwt = currentJwt();
        return ApiResponse.success(service.findByIdForUser(id, currentUserId(jwt), hasRole(jwt, "ADMIN")));
    }

    @Override
    @PostMapping
    public ResponseEntity<ApiResponse<StudentApplication>> create(
            @Valid @RequestBody StudentApplication body
    ) {
        Jwt jwt = currentJwt();
        StudentApplication application = hasRole(jwt, "ADMIN")
                ? service.create(body)
                : service.createForUser(body, currentUserId(jwt));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo mới thành công", application));
    }

    @GetMapping("/search")
    public ApiResponse<List<StudentApplication>> search(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status
    ) {
        Jwt jwt = currentJwt();
        if (!hasRole(jwt, "ADMIN")) {
            Long currentUserId = currentUserId(jwt);
            if (userId != null && !userId.equals(currentUserId)) {
                throw new AccessDeniedException("You cannot view another user's applications");
            }
            return ApiResponse.success(filterByStatus(service.findByUserId(currentUserId), status));
        }
        if (userId != null) {
            return ApiResponse.success(service.findByUserId(userId));
        }
        if (status != null && !status.isBlank()) {
            return ApiResponse.success(service.findByStatus(status));
        }
        return ApiResponse.success(service.findAll());
    }

    private List<StudentApplication> filterByStatus(List<StudentApplication> applications, String status) {
        if (status == null || status.isBlank()) {
            return applications;
        }
        return applications.stream()
                .filter(application -> status.equalsIgnoreCase(application.getStatus()))
                .toList();
    }

    private Jwt currentJwt() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Jwt jwt) {
            return jwt;
        }
        throw new AccessDeniedException("Authenticated user is required");
    }

    private Long currentUserId(Jwt jwt) {
        Object userId = jwt.getClaim("userId");
        if (userId instanceof Number number) {
            return number.longValue();
        }
        return Long.valueOf(userId.toString());
    }

    private boolean hasRole(Jwt jwt, String role) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        return roles != null && roles.contains(role);
    }
}
