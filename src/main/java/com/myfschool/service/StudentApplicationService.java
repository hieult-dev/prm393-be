package com.myfschool.service;

import com.myfschool.entity.StudentApplication;
import com.myfschool.repository.StudentApplicationRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentApplicationService extends AbstractCrudService<StudentApplication> {

    private final StudentApplicationRepository repository;

    public StudentApplicationService(StudentApplicationRepository repository) {
        super(repository, "Student application");
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<StudentApplication> findByUserId(Long userId) {
        return repository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<StudentApplication> findByStatus(String status) {
        return repository.findByStatus(status);
    }
}
