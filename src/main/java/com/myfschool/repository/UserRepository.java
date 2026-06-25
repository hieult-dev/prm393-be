package com.myfschool.repository;

import com.myfschool.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByStudentCode(String studentCode);

    Optional<User> findByPhone(String phone);
}
