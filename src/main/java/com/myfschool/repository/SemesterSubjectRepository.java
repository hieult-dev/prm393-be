package com.myfschool.repository;

import com.myfschool.entity.SemesterSubject;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SemesterSubjectRepository extends JpaRepository<SemesterSubject, Long> {

    List<SemesterSubject> findBySemesterIdOrderByStartDateAscIdAsc(Long semesterId);

    List<SemesterSubject> findBySubjectIdOrderByStartDateAscIdAsc(Long subjectId);

    List<SemesterSubject> findBySemesterIdAndSubjectIdOrderByStartDateAscIdAsc(Long semesterId, Long subjectId);

    boolean existsBySemesterIdAndSubjectId(Long semesterId, Long subjectId);
}
