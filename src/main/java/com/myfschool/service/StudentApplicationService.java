package com.myfschool.service;

import com.myfschool.entity.StudentApplication;
import com.myfschool.exception.ResourceNotFoundException;
import com.myfschool.repository.StudentApplicationRepository;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
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

    @Transactional(readOnly = true)
    public StudentApplication findByIdForUser(Long id, Long currentUserId, boolean admin) {
        StudentApplication application = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student application", id));
        if (!admin && !application.getUserId().equals(currentUserId)) {
            throw new AccessDeniedException("You cannot view another user's application");
        }
        return application;
    }

    @Transactional
    public StudentApplication createForUser(StudentApplication application, Long currentUserId) {
        application.setId(null);
        application.setUserId(currentUserId);
        application.setStatus("PENDING");
        application.setResponseNote(null);
        return repository.save(application);
    }
}
