package com.myfschool.repository;

import com.myfschool.entity.TeacherSubject;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherSubjectRepository extends JpaRepository<TeacherSubject, Long> {

    List<TeacherSubject> findByTeacherIdOrderByIdAsc(Long teacherId);

    List<TeacherSubject> findBySubjectIdInOrderByTeacherIdAsc(Collection<Long> subjectIds);

    boolean existsByTeacherIdAndSubjectId(Long teacherId, Long subjectId);
}
