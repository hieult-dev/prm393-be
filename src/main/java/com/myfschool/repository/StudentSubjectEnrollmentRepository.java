package com.myfschool.repository;

import com.myfschool.entity.StudentSubjectEnrollment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentSubjectEnrollmentRepository extends JpaRepository<StudentSubjectEnrollment, Long> {

    List<StudentSubjectEnrollment> findByUserIdAndSemesterIdOrderByIdAsc(Long userId, Long semesterId);

    boolean existsByUserIdAndSemesterIdAndSubjectId(Long userId, Long semesterId, Long subjectId);
}
