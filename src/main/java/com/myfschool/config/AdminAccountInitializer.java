package com.myfschool.config;

import com.myfschool.entity.Role;
import com.myfschool.entity.User;
import com.myfschool.repository.RoleRepository;
import com.myfschool.repository.UserRepository;
import java.util.LinkedHashSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminAccountInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final String userName;
    private final String password;
    private final String email;

    public AdminAccountInitializer(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.username}") String userName,
            @Value("${app.admin.password}") String password,
            @Value("${app.admin.email}") String email
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userName = userName;
        this.password = password;
        this.email = email;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.existsByUserName(userName)) {
            return;
        }

        Role adminRole = roleRepository.findByRoleName("ADMIN")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setRoleName("ADMIN");
                    return roleRepository.save(role);
                });
        User admin = new User();
        admin.setUserName(userName);
        admin.setUserPassword(passwordEncoder.encode(password));
        admin.setFirstName("System");
        admin.setLastName("Administrator");
        admin.setEmail(email);
        admin.setStatus("ACTIVE");
        admin.setRoles(new LinkedHashSet<>(java.util.List.of(adminRole)));
        userRepository.save(admin);
    }
}
