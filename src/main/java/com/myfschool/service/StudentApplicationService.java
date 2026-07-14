package com.myfschool.service;

import com.myfschool.dto.request.ReviewStudentApplicationRequest;
import com.myfschool.entity.ApplicationType;
import com.myfschool.entity.StudentApplication;
import com.myfschool.entity.User;
import com.myfschool.exception.BadRequestException;
import com.myfschool.exception.ResourceNotFoundException;
import com.myfschool.repository.ApplicationTypeRepository;
import com.myfschool.repository.StudentApplicationRepository;
import com.myfschool.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentApplicationService extends AbstractCrudService<StudentApplication> {

    private static final Set<String> REVIEW_STATUSES = Set.of("APPROVED", "REJECTED");

    private final StudentApplicationRepository repository;
    private final UserRepository userRepository;
    private final ApplicationTypeRepository applicationTypeRepository;

    public StudentApplicationService(
            StudentApplicationRepository repository,
            UserRepository userRepository,
            ApplicationTypeRepository applicationTypeRepository
    ) {
        super(repository, "Student application");
        this.repository = repository;
        this.userRepository = userRepository;
        this.applicationTypeRepository = applicationTypeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentApplication> findAll() {
        return enrich(repository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public StudentApplication findById(Long id) {
        return enrich(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student application", id)));
    }

    @Override
    @Transactional
    public StudentApplication create(StudentApplication application) {
        application.setId(null);
        StudentApplication saved = repository.save(application);
        return enrich(saved);
    }

    @Transactional(readOnly = true)
    public List<StudentApplication> findByUserId(Long userId) {
        return enrich(repository.findByUserId(userId));
    }

    @Transactional(readOnly = true)
    public List<StudentApplication> findByStatus(String status) {
        return enrich(repository.findByStatus(status));
    }

    @Transactional(readOnly = true)
    public StudentApplication findByIdForUser(Long id, Long currentUserId, boolean admin) {
        StudentApplication application = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student application", id));
        if (!admin && !application.getUserId().equals(currentUserId)) {
            throw new AccessDeniedException("You cannot view another user's application");
        }
        return enrich(application);
    }

    @Transactional
    public StudentApplication createForUser(StudentApplication application, Long currentUserId) {
        application.setId(null);
        application.setUserId(currentUserId);
        application.setStatus("PENDING");
        application.setResponseNote(null);
        return enrich(repository.save(application));
    }

    @Transactional
    public StudentApplication review(Long id, ReviewStudentApplicationRequest request) {
        StudentApplication application = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student application", id));
        String status = request.status().trim().toUpperCase();
        if (!REVIEW_STATUSES.contains(status)) {
            throw new BadRequestException("Application status must be APPROVED or REJECTED");
        }
        application.setStatus(status);
        application.setResponseNote(normalizeResponseNote(request.responseNote()));
        application.setUpdatedAt(LocalDateTime.now());
        return enrich(repository.save(application));
    }

    private String normalizeResponseNote(String responseNote) {
        if (responseNote == null) {
            return null;
        }
        String normalized = responseNote.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private List<StudentApplication> enrich(List<StudentApplication> applications) {
        applications.forEach(this::enrich);
        return applications;
    }

    private StudentApplication enrich(StudentApplication application) {
        userRepository.findById(application.getUserId()).ifPresent(user -> {
            application.setStudentCode(user.getUserName());
            application.setStudentName(fullName(user));
            application.setClassName(user.getClassName());
        });
        applicationTypeRepository.findById(application.getApplicationTypeId())
                .map(ApplicationType::getName)
                .ifPresent(application::setApplicationTypeName);
        return application;
    }

    private String fullName(User user) {
        String firstName = user.getFirstName() == null ? "" : user.getFirstName().trim();
        String lastName = user.getLastName() == null ? "" : user.getLastName().trim();
        String name = (firstName + " " + lastName).trim();
        return name.isBlank() ? user.getUserName() : name;
    }
}
