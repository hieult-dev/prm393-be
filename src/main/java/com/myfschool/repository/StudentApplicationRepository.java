package com.myfschool.repository;

import com.myfschool.entity.StudentApplication;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentApplicationRepository extends JpaRepository<StudentApplication, Long> {

    List<StudentApplication> findByUserId(Long userId);

    List<StudentApplication> findByStatus(String status);
}
