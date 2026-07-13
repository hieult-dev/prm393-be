package com.myfschool.repository;

import com.myfschool.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUserName(String userName);

    Optional<User> findByPhone(String phone);

    boolean existsByUserName(String userName);

    boolean existsByEmail(String email);

    List<User> findDistinctByRolesRoleNameOrderByUserNameAsc(String roleName);
}
