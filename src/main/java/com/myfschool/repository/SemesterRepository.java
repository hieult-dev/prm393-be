package com.myfschool.repository;

import com.myfschool.entity.Semester;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SemesterRepository extends JpaRepository<Semester, Long> {
}
