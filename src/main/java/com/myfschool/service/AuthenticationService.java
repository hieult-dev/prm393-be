package com.myfschool.service;

import com.myfschool.dto.request.LoginRequest;
import com.myfschool.dto.request.LogoutRequest;
import com.myfschool.dto.request.RefreshTokenRequest;
import com.myfschool.dto.request.RegisterRequest;
import com.myfschool.dto.response.AuthResponse;
import com.myfschool.dto.response.RefreshTokenResult;
import com.myfschool.entity.Role;
import com.myfschool.entity.User;
import com.myfschool.exception.InvalidCredentialsException;
import com.myfschool.exception.ResourceAlreadyExistsException;
import com.myfschool.exception.ResourceNotFoundException;
import com.myfschool.repository.RoleRepository;
import com.myfschool.repository.UserRepository;
import java.util.LinkedHashSet;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;

    public AuthenticationService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            UserService userService
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.userService = userService;
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUserName(request.userName().trim())
                .orElseThrow(InvalidCredentialsException::new);
        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())
                || !passwordEncoder.matches(request.password(), user.getUserPassword())) {
            throw new InvalidCredentialsException();
        }

        if (passwordEncoder.upgradeEncoding(user.getUserPassword())) {
            user.setUserPassword(passwordEncoder.encode(request.password()));
            userRepository.save(user);
        }
        return buildResponse(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshTokenResult refreshToken = refreshTokenService.rotate(request.refreshToken());
        return buildResponse(refreshToken.user(), refreshToken);
    }

    @Transactional
    public void logout(LogoutRequest request) {
        refreshTokenService.revoke(request.refreshToken());
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String userName = request.userName().trim();
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByUserName(userName)) {
            throw new ResourceAlreadyExistsException("Tên đăng nhập đã tồn tại");
        }
        if (userRepository.existsByEmail(email)) {
            throw new ResourceAlreadyExistsException("Email đã tồn tại");
        }

        Role studentRole = roleRepository.findByRoleName("STUDENT")
                .orElseThrow(() -> new ResourceNotFoundException("Role", "roleName", "STUDENT"));
        User user = new User();
        user.setUserName(userName);
        user.setUserPassword(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName().trim());
        user.setLastName(trimToNull(request.lastName()));
        user.setEmail(email);
        user.setPhone(trimToNull(request.phone()));
        user.setClassName(trimToNull(request.className()));
        user.setStatus("ACTIVE");
        user.setRoles(new LinkedHashSet<>(java.util.List.of(studentRole)));

        return buildResponse(userRepository.save(user));
    }

    private AuthResponse buildResponse(User user) {
        return buildResponse(user, refreshTokenService.createForUser(user));
    }

    private AuthResponse buildResponse(User user, RefreshTokenResult refreshToken) {
        return new AuthResponse(
                jwtService.generateToken(user),
                refreshToken.token(),
                "Bearer",
                jwtService.getExpirationSeconds(),
                refreshToken.expiresIn(),
                userService.mapToLoginResponse(user)
        );
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
