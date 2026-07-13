package com.myfschool.repository;

import com.myfschool.entity.StudentGradeItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentGradeItemRepository extends JpaRepository<StudentGradeItem, Long> {

    List<StudentGradeItem> findByStudentGradeIdOrderByDisplayOrderAscIdAsc(Long studentGradeId);

    void deleteByStudentGradeId(Long studentGradeId);
}
