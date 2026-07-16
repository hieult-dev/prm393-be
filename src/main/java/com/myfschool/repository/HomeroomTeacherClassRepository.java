package com.myfschool.repository;

import com.myfschool.entity.HomeroomTeacherClass;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HomeroomTeacherClassRepository extends JpaRepository<HomeroomTeacherClass, Long> {

    Optional<HomeroomTeacherClass> findByTeacherId(Long teacherId);
}
