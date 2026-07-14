package com.myfschool.repository;

import com.myfschool.entity.Schedule;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByUserId(Long userId);

    List<Schedule> findByUserIdAndStudyDate(Long userId, LocalDate studyDate);

    List<Schedule> findByUserIdAndStudyDateBetweenOrderByStudyDateAscStartTimeAsc(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    );

    List<Schedule> findByUserIdAndSemesterIdOrderByStudyDateAscStartTimeAsc(
            Long userId,
            Long semesterId
    );

    List<Schedule> findByUserIdAndSemesterIdAndStudyDateOrderByStartTimeAsc(
            Long userId,
            Long semesterId,
            LocalDate studyDate
    );

    List<Schedule> findByUserIdAndStudyDateOrderByStartTimeAsc(Long userId, LocalDate studyDate);

    List<Schedule> findByTeacherIdOrderByStudyDateAscStartTimeAsc(Long teacherId);

    List<Schedule> findByTeacherIdAndStudyDateOrderByStartTimeAsc(Long teacherId, LocalDate studyDate);

    List<Schedule> findByTeacherIdAndStudyDateBetweenOrderByStudyDateAscStartTimeAsc(
            Long teacherId,
            LocalDate startDate,
            LocalDate endDate
    );

    List<Schedule> findByTeacherIdAndSemesterIdOrderByStudyDateAscStartTimeAsc(
            Long teacherId,
            Long semesterId
    );

    List<Schedule> findByTeacherIdAndSemesterIdAndStudyDateOrderByStartTimeAsc(
            Long teacherId,
            Long semesterId,
            LocalDate studyDate
    );
}
