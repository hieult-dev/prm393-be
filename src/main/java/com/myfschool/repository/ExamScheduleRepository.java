package com.myfschool.repository;

import com.myfschool.entity.ExamSchedule;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExamScheduleRepository extends JpaRepository<ExamSchedule, Long> {

    List<ExamSchedule> findAllByOrderByExamDateAscStartTimeAsc();

    List<ExamSchedule> findByUserIdOrderByExamDateAscStartTimeAsc(Long userId);

    List<ExamSchedule> findByUserIdAndSemesterIdOrderByExamDateAscStartTimeAsc(
            Long userId,
            Long semesterId
    );

    List<ExamSchedule> findByUserIdAndExamDateOrderByStartTimeAsc(Long userId, LocalDate examDate);

    Optional<ExamSchedule> findByUserIdAndSubjectIdAndSemesterIdAndExamType(
            Long userId,
            Long subjectId,
            Long semesterId,
            String examType
    );

    @Query("""
            SELECT examSchedule
            FROM ExamSchedule examSchedule
            WHERE (:userId IS NULL OR examSchedule.userId = :userId)
              AND (:semesterId IS NULL OR examSchedule.semesterId = :semesterId)
              AND (:examDate IS NULL OR examSchedule.examDate = :examDate)
              AND (:status IS NULL OR LOWER(examSchedule.status) = LOWER(:status))
            ORDER BY examSchedule.examDate ASC, examSchedule.startTime ASC
            """)
    List<ExamSchedule> search(
            @Param("userId") Long userId,
            @Param("semesterId") Long semesterId,
            @Param("examDate") LocalDate examDate,
            @Param("status") String status
    );
}
