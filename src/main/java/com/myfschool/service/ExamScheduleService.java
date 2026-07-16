package com.myfschool.service;

import com.myfschool.dto.response.ExamScheduleItemResponse;
import com.myfschool.entity.ExamSchedule;
import com.myfschool.entity.Semester;
import com.myfschool.entity.Subject;
import com.myfschool.entity.User;
import com.myfschool.exception.BadRequestException;
import com.myfschool.exception.ResourceNotFoundException;
import com.myfschool.repository.ExamScheduleRepository;
import com.myfschool.repository.SemesterRepository;
import com.myfschool.repository.StudentSubjectEnrollmentRepository;
import com.myfschool.repository.SubjectRepository;
import com.myfschool.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExamScheduleService extends AbstractCrudService<ExamSchedule> {

    private final ExamScheduleRepository repository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final SemesterRepository semesterRepository;
    private final StudentSubjectEnrollmentRepository enrollmentRepository;

    public ExamScheduleService(
            ExamScheduleRepository repository,
            UserRepository userRepository,
            SubjectRepository subjectRepository,
            SemesterRepository semesterRepository,
            StudentSubjectEnrollmentRepository enrollmentRepository
    ) {
        super(repository, "Exam schedule");
        this.repository = repository;
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
        this.semesterRepository = semesterRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExamSchedule> findAll() {
        return repository.findAllByOrderByExamDateAscStartTimeAsc();
    }

    @Transactional(readOnly = true)
    public List<ExamScheduleItemResponse> findForStudent(Long userId, Long semesterId) {
        requireStudent(userId);
        List<ExamSchedule> schedules = repository.search(userId, semesterId, null, "PUBLISHED");
        return schedules.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExamSchedule> search(Long userId, Long semesterId, LocalDate examDate, String status) {
        return repository.search(userId, semesterId, examDate, normalizeOptional(status));
    }

    @Override
    @Transactional
    public ExamSchedule create(ExamSchedule examSchedule) {
        validate(examSchedule, null);
        examSchedule.setId(null);
        return repository.save(examSchedule);
    }

    @Override
    @Transactional
    public ExamSchedule update(Long id, ExamSchedule examSchedule) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Exam schedule", id);
        }
        validate(examSchedule, id);
        examSchedule.setId(id);
        return repository.save(examSchedule);
    }

    private void validate(ExamSchedule examSchedule, Long currentId) {
        requireStudent(examSchedule.getUserId());
        Subject subject = subjectRepository.findById(examSchedule.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", examSchedule.getSubjectId()));
        Semester semester = semesterRepository.findById(examSchedule.getSemesterId())
                .orElseThrow(() -> new ResourceNotFoundException("Semester", examSchedule.getSemesterId()));

        if (!enrollmentRepository.existsByUserIdAndSemesterIdAndSubjectId(
                examSchedule.getUserId(),
                semester.getId(),
                subject.getId()
        )) {
            throw new BadRequestException("Student is not enrolled in this subject for the selected semester");
        }
        if (examSchedule.getExamDate().isBefore(semester.getStartDate())
                || examSchedule.getExamDate().isAfter(semester.getEndDate())) {
            throw new BadRequestException("Exam date must be within the selected semester");
        }
        if (!examSchedule.getEndTime().isAfter(examSchedule.getStartTime())) {
            throw new BadRequestException("Exam end time must be after start time");
        }

        examSchedule.setExamType(normalizeRequired(examSchedule.getExamType(), "FINAL"));
        examSchedule.setStatus(normalizeRequired(examSchedule.getStatus(), "PUBLISHED"));

        boolean duplicatedExamType = repository
                .findByUserIdAndSubjectIdAndSemesterIdAndExamType(
                        examSchedule.getUserId(),
                        examSchedule.getSubjectId(),
                        examSchedule.getSemesterId(),
                        examSchedule.getExamType()
                )
                .filter(existing -> currentId == null || !existing.getId().equals(currentId))
                .isPresent();
        if (duplicatedExamType) {
            throw new BadRequestException("Student already has this exam type for the selected subject");
        }

        boolean overlaps = repository
                .findByUserIdAndExamDateOrderByStartTimeAsc(
                        examSchedule.getUserId(),
                        examSchedule.getExamDate()
                )
                .stream()
                .filter(existing -> currentId == null || !existing.getId().equals(currentId))
                .anyMatch(existing -> examSchedule.getStartTime().isBefore(existing.getEndTime())
                        && examSchedule.getEndTime().isAfter(existing.getStartTime()));
        if (overlaps) {
            throw new BadRequestException("Student already has an exam in this time range");
        }
    }

    private ExamScheduleItemResponse toResponse(ExamSchedule examSchedule) {
        Subject subject = subjectRepository.findById(examSchedule.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", examSchedule.getSubjectId()));
        Semester semester = semesterRepository.findById(examSchedule.getSemesterId())
                .orElseThrow(() -> new ResourceNotFoundException("Semester", examSchedule.getSemesterId()));
        return new ExamScheduleItemResponse(
                examSchedule.getId(),
                semester.getId(),
                semester.getName(),
                subject.getId(),
                subject.getSubjectCode(),
                subject.getSubjectName(),
                examSchedule.getExamType(),
                examSchedule.getExamDate(),
                examSchedule.getStartTime(),
                examSchedule.getEndTime(),
                examSchedule.getRoom(),
                examSchedule.getSeatNumber(),
                examSchedule.getProctorName(),
                examSchedule.getNote(),
                examSchedule.getStatus()
        );
    }

    private User requireStudent(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        if (!hasRole(user, "STUDENT")) {
            throw new BadRequestException("Selected user is not a student");
        }
        return user;
    }

    private boolean hasRole(User user, String roleName) {
        return user.getRoles().stream()
                .anyMatch(role -> roleName.equals(role.getRoleName()));
    }

    private String normalizeRequired(String value, String fallback) {
        String normalized = normalizeOptional(value);
        return normalized == null ? fallback : normalized;
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }
}
