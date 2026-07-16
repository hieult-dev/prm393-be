package com.myfschool.service;

import com.myfschool.dto.request.ReviewStudentApplicationRequest;
import com.myfschool.entity.ApplicationType;
import com.myfschool.entity.HomeroomTeacherClass;
import com.myfschool.entity.StudentApplication;
import com.myfschool.entity.User;
import com.myfschool.exception.BadRequestException;
import com.myfschool.exception.ResourceNotFoundException;
import com.myfschool.repository.ApplicationTypeRepository;
import com.myfschool.repository.HomeroomTeacherClassRepository;
import com.myfschool.repository.ParentStudentRepository;
import com.myfschool.repository.ScheduleRepository;
import com.myfschool.repository.StudentApplicationRepository;
import com.myfschool.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentApplicationService extends AbstractCrudService<StudentApplication> {

    private static final Set<String> REVIEW_STATUSES = Set.of("APPROVED", "REJECTED");
    private static final Set<String> FILTER_STATUSES = Set.of("PENDING", "APPROVED", "REJECTED");

    private final StudentApplicationRepository repository;
    private final UserRepository userRepository;
    private final ApplicationTypeRepository applicationTypeRepository;
    private final ParentStudentRepository parentStudentRepository;
    private final HomeroomTeacherClassRepository homeroomTeacherClassRepository;
    private final ScheduleRepository scheduleRepository;

    public StudentApplicationService(
            StudentApplicationRepository repository,
            UserRepository userRepository,
            ApplicationTypeRepository applicationTypeRepository,
            ParentStudentRepository parentStudentRepository,
            HomeroomTeacherClassRepository homeroomTeacherClassRepository,
            ScheduleRepository scheduleRepository
    ) {
        super(repository, "Student application");
        this.repository = repository;
        this.userRepository = userRepository;
        this.applicationTypeRepository = applicationTypeRepository;
        this.parentStudentRepository = parentStudentRepository;
        this.homeroomTeacherClassRepository = homeroomTeacherClassRepository;
        this.scheduleRepository = scheduleRepository;
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
        validateApplicationType(application.getApplicationTypeId());
        return enrich(repository.save(application));
    }

    @Transactional(readOnly = true)
    public List<StudentApplication> findByUserId(Long userId) {
        return enrich(repository.findByUserIdOrderByCreatedAtDesc(userId));
    }

    @Transactional(readOnly = true)
    public List<StudentApplication> findByStatus(String status) {
        String normalizedStatus = normalizeFilterStatus(status);
        if (normalizedStatus == null) {
            return findAll();
        }
        return enrich(repository.findByStatusOrderByCreatedAtDesc(normalizedStatus));
    }

    @Transactional(readOnly = true)
    public List<StudentApplication> findForParent(Long parentId, Long studentId, String status) {
        requireParent(parentId);
        String normalizedStatus = normalizeFilterStatus(status);
        if (studentId != null) {
            requireLinkedStudent(parentId, studentId);
            return enrich(repository.findForParentStudent(parentId, studentId, normalizedStatus));
        }
        return enrich(repository.findForParent(parentId, normalizedStatus));
    }

    @Transactional(readOnly = true)
    public List<StudentApplication> findForHomeroomTeacher(Long teacherId, String status) {
        HomeroomTeacherClass homeroom = requireHomeroomTeacherClass(teacherId);
        return enrich(repository.findForHomeroomClass(
                homeroom.getClassName(),
                normalizeFilterStatus(status)
        ));
    }

    @Transactional(readOnly = true)
    public StudentApplication findByIdForUser(Long id, Long currentUserId, boolean homeroomTeacher) {
        StudentApplication application = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student application", id));
        if (homeroomTeacher) {
            requireHomeroomAccess(currentUserId, application);
        } else if (!application.getUserId().equals(currentUserId)
                && !currentUserId.equals(application.getParentId())) {
            throw new AccessDeniedException("You cannot view another user's application");
        }
        return enrich(application);
    }

    @Transactional
    public StudentApplication createForParent(StudentApplication application, Long parentId, Long studentId) {
        requireLinkedStudent(parentId, studentId);
        validateApplicationType(application.getApplicationTypeId());
        application.setId(null);
        application.setUserId(studentId);
        application.setParentId(parentId);
        application.setStatus("PENDING");
        application.setResponseNote(null);
        application.setUpdatedAt(null);
        return enrich(repository.save(application));
    }

    @Transactional
    public StudentApplication createForUser(StudentApplication application, Long currentUserId) {
        throw new AccessDeniedException("Students cannot submit applications directly");
    }

    @Transactional
    public StudentApplication review(Long id, ReviewStudentApplicationRequest request) {
        StudentApplication application = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student application", id));
        return reviewApplication(application, request);
    }

    @Transactional
    public StudentApplication reviewForHomeroomTeacher(
            Long id,
            ReviewStudentApplicationRequest request,
            Long teacherId
    ) {
        StudentApplication application = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student application", id));
        requireHomeroomAccess(teacherId, application);
        return reviewApplication(application, request);
    }

    private StudentApplication reviewApplication(
            StudentApplication application,
            ReviewStudentApplicationRequest request
    ) {
        String status = request.status().trim().toUpperCase(Locale.ROOT);
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

    private String normalizeFilterStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        String normalized = status.trim().toUpperCase(Locale.ROOT);
        if (!FILTER_STATUSES.contains(normalized)) {
            throw new BadRequestException("Application status filter is invalid");
        }
        return normalized;
    }

    private void validateApplicationType(Long applicationTypeId) {
        if (applicationTypeId == null || !applicationTypeRepository.existsById(applicationTypeId)) {
            throw new ResourceNotFoundException("Application type", applicationTypeId);
        }
    }

    private void requireLinkedStudent(Long parentId, Long studentId) {
        requireParent(parentId);
        requireStudent(studentId);
        if (!parentStudentRepository.existsByParentIdAndStudentId(parentId, studentId)) {
            throw new AccessDeniedException("You cannot submit applications for an unlinked student");
        }
    }

    private User requireParent(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        if (!hasRole(user, "PARENT")) {
            throw new BadRequestException("Selected user is not a parent");
        }
        return user;
    }

    private User requireStudent(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        if (!hasRole(user, "STUDENT")) {
            throw new BadRequestException("Selected user is not a student");
        }
        return user;
    }

    private HomeroomTeacherClass requireHomeroomTeacherClass(Long teacherId) {
        User user = userRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("User", teacherId));
        if (!hasRole(user, "HOMEROOM_TEACHER")) {
            throw new AccessDeniedException("Only homeroom teachers can access applications");
        }
        HomeroomTeacherClass homeroom = homeroomTeacherClassRepository.findByTeacherId(teacherId)
                .orElseThrow(() -> new BadRequestException("Homeroom teacher has not been assigned a class"));
        String className = homeroom.getClassName() == null ? "" : homeroom.getClassName().trim();
        if (className.isBlank() || scheduleRepository.countTeachingAssignmentsForClass(teacherId, className) == 0) {
            throw new AccessDeniedException("Homeroom teacher must teach at least one subject in the assigned class");
        }
        return homeroom;
    }

    private void requireHomeroomAccess(Long teacherId, StudentApplication application) {
        HomeroomTeacherClass homeroom = requireHomeroomTeacherClass(teacherId);
        User student = requireStudent(application.getUserId());
        String studentClass = student.getClassName() == null ? "" : student.getClassName().trim();
        if (!studentClass.equalsIgnoreCase(homeroom.getClassName().trim())) {
            throw new AccessDeniedException("You cannot review applications outside your homeroom class");
        }
    }

    private boolean hasRole(User user, String roleName) {
        return user.getRoles().stream()
                .anyMatch(role -> roleName.equals(role.getRoleName()));
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
        if (application.getParentId() != null) {
            userRepository.findById(application.getParentId()).ifPresent(parent -> {
                application.setParentUserName(parent.getUserName());
                application.setParentName(fullName(parent));
            });
        }
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
