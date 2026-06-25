package com.myfschool.service;

import com.myfschool.dto.request.LoginRequest;
import com.myfschool.dto.response.LoginResponse;
import com.myfschool.entity.User;
import com.myfschool.exception.InvalidCredentialsException;
import com.myfschool.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService extends AbstractCrudService<User> {

    private final UserRepository userRepository;

    public UserService(UserRepository repository) {
        super(repository, "User");
        this.userRepository = repository;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByPhone(loginRequest.phone())
                .orElseThrow(InvalidCredentialsException::new);

        if (!user.getPasswordHash().equals(loginRequest.password())) {
            throw new InvalidCredentialsException();
        }
        
        return mapToLoginResponse(user);
    }

    private LoginResponse mapToLoginResponse(User user) {
        return new LoginResponse(
                user.getId(),
                user.getStudentCode(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getClassName(),
                user.getRole(),
                user.getStatus()
        );
    }
}
