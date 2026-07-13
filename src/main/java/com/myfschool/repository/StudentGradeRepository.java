package com.myfschool.repository;

import com.myfschool.entity.StudentGrade;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentGradeRepository extends JpaRepository<StudentGrade, Long> {

    List<StudentGrade> findByUserId(Long userId);

    List<StudentGrade> findByUserIdAndSemesterId(Long userId, Long semesterId);

    Optional<StudentGrade> findByUserIdAndSubjectIdAndSemesterId(
            Long userId,
            Long subjectId,
            Long semesterId
    );
}
