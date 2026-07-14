package com.myfschool.repository;

import com.myfschool.entity.ParentStudent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParentStudentRepository extends JpaRepository<ParentStudent, Long> {

    List<ParentStudent> findByParentIdOrderByIdAsc(Long parentId);

    boolean existsByParentIdAndStudentId(Long parentId, Long studentId);
}
