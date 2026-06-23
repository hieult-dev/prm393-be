package com.myfschool.service;

import com.myfschool.entity.StudentGrade;
import com.myfschool.repository.StudentGradeRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentGradeService extends AbstractCrudService<StudentGrade> {

    private final StudentGradeRepository repository;

    public StudentGradeService(StudentGradeRepository repository) {
        super(repository, "Student grade");
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<StudentGrade> findByUserId(Long userId) {
        return repository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<StudentGrade> findByUserIdAndSemesterId(Long userId, Long semesterId) {
        return repository.findByUserIdAndSemesterId(userId, semesterId);
    }
}
