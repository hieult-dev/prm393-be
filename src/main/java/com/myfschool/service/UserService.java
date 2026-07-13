package com.myfschool.service;

import com.myfschool.dto.request.LoginRequest;
import com.myfschool.dto.response.LoginResponse;
import com.myfschool.entity.Permission;
import com.myfschool.entity.Role;
import com.myfschool.entity.User;
import com.myfschool.exception.InvalidCredentialsException;
import com.myfschool.exception.ResourceNotFoundException;
import com.myfschool.repository.RoleRepository;
import com.myfschool.repository.UserRepository;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService extends AbstractCrudService<User> {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserService(UserRepository repository, RoleRepository roleRepository) {
        super(repository, "User");
        this.userRepository = repository;
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public User create(User user) {
        user.setRoles(resolveRoles(user));
        return super.create(user);
    }

    @Override
    @Transactional
    public User update(Long id, User user) {
        user.setRoles(resolveRoles(user));
        return super.update(id, user);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByUserName(loginRequest.userName())
                .orElseThrow(InvalidCredentialsException::new);

        if (!user.getUserPassword().equals(loginRequest.password())) {
            throw new InvalidCredentialsException();
        }
        
        return mapToLoginResponse(user);
    }

    private LoginResponse mapToLoginResponse(User user) {
        List<String> roles = user.getRoles().stream()
                .map(Role::getRoleName)
                .sorted()
                .toList();
        List<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getPermissionName)
                .distinct()
                .sorted()
                .toList();

        return new LoginResponse(
                user.getId(),
                user.getUserName(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getClassName(),
                roles.stream().findFirst().orElse(null),
                roles,
                permissions,
                user.getStatus()
        );
    }

    private Set<Role> resolveRoles(User user) {
        Set<Role> roles = new LinkedHashSet<>();

        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            user.getRoles().forEach(role -> roles.add(resolveRole(role)));
        }

        if (roles.isEmpty() && user.getRequestedRole() != null && !user.getRequestedRole().isBlank()) {
            roles.add(findOrCreateRole(user.getRequestedRole()));
        }

        if (roles.isEmpty()) {
            roles.add(findOrCreateRole("STUDENT"));
        }

        return roles;
    }

    private Role resolveRole(Role role) {
        if (role.getId() != null) {
            return roleRepository.findById(role.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Role", role.getId()));
        }

        if (role.getRoleName() == null || role.getRoleName().isBlank()) {
            throw new ResourceNotFoundException("Role", "roleName", role.getRoleName());
        }

        return findOrCreateRole(role.getRoleName());
    }

    private Role findOrCreateRole(String roleName) {
        String normalizedRoleName = roleName.trim().toUpperCase();
        return roleRepository.findByRoleName(normalizedRoleName)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setRoleName(normalizedRoleName);
                    return roleRepository.save(role);
                });
    }
}
