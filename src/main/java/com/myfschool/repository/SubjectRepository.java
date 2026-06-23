package com.myfschool.repository;

import com.myfschool.entity.Subject;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    Optional<Subject> findBySubjectCode(String subjectCode);
}
