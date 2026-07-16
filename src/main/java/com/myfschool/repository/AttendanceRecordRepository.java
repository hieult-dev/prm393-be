package com.myfschool.repository;

import com.myfschool.entity.AttendanceRecord;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    List<AttendanceRecord> findByUserIdOrderBySemesterIdAscSubjectIdAscAttendanceDateAsc(Long userId);

    List<AttendanceRecord> findByUserIdAndSemesterIdOrderBySubjectIdAscAttendanceDateAsc(
            Long userId,
            Long semesterId
    );
}
