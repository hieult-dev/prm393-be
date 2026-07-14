package com.myfschool.service;

import com.myfschool.dto.response.AdminParentResponse;
import com.myfschool.dto.response.AdminStudentResponse;
import com.myfschool.dto.response.MarkDetailResponse;
import com.myfschool.dto.response.MarkReportSemesterResponse;
import com.myfschool.dto.response.ScheduleItemResponse;
import com.myfschool.entity.ParentStudent;
import com.myfschool.entity.User;
import com.myfschool.exception.BadRequestException;
import com.myfschool.exception.ResourceNotFoundException;
import com.myfschool.repository.ParentStudentRepository;
import com.myfschool.repository.UserRepository;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ParentStudentService {

    private final ParentStudentRepository repository;
    private final UserRepository userRepository;
    private final StudentGradeService studentGradeService;
    private final ScheduleService scheduleService;

    public ParentStudentService(
            ParentStudentRepository repository,
            UserRepository userRepository,
            StudentGradeService studentGradeService,
            ScheduleService scheduleService
    ) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.studentGradeService = studentGradeService;
        this.scheduleService = scheduleService;
    }

    @Transactional(readOnly = true)
    public List<AdminParentResponse> getParents(String search) {
        String keyword = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        return userRepository.findDistinctByRolesRoleNameOrderByUserNameAsc("PARENT").stream()
                .map(this::mapParent)
                .filter(parent -> matchesParent(parent, keyword))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminStudentResponse> getLinkedStudents(Long parentId) {
        requireParent(parentId);
        Set<Long> studentIds = repository.findByParentIdOrderByIdAsc(parentId)
                .stream()
                .map(ParentStudent::getStudentId)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        Map<Long, User> studentsById = userRepository.findAllById(studentIds)
                .stream()
                .collect(java.util.stream.Collectors.toMap(User::getId, Function.identity()));

        return studentIds.stream()
                .map(studentsById::get)
                .filter(student -> student != null)
                .map(this::mapStudent)
                .toList();
    }

    @Transactional
    public List<AdminStudentResponse> assignStudents(Long parentId, List<Long> studentIds) {
        requireParent(parentId);

        Set<Long> selectedStudentIds = new HashSet<>(studentIds);
        Map<Long, User> selectedStudentsById = userRepository.findAllById(selectedStudentIds)
                .stream()
                .collect(java.util.stream.Collectors.toMap(User::getId, Function.identity()));
        if (!selectedStudentsById.keySet().containsAll(selectedStudentIds)) {
            throw new BadRequestException("Student not found in selected parent links");
        }
        selectedStudentsById.values().forEach(this::requireStudent);

        List<ParentStudent> current = repository.findByParentIdOrderByIdAsc(parentId);
        repository.deleteAll(current.stream()
                .filter(link -> !selectedStudentIds.contains(link.getStudentId()))
                .toList());

        Set<Long> currentStudentIds = current.stream()
                .map(ParentStudent::getStudentId)
                .collect(java.util.stream.Collectors.toSet());
        selectedStudentIds.stream()
                .filter(studentId -> !currentStudentIds.contains(studentId))
                .forEach(studentId -> {
                    ParentStudent link = new ParentStudent();
                    link.setParentId(parentId);
                    link.setStudentId(studentId);
                    repository.save(link);
                });

        return getLinkedStudents(parentId);
    }

    @Transactional(readOnly = true)
    public List<MarkReportSemesterResponse> getChildMarkReport(Long parentId, Long studentId) {
        requireLinkedStudent(parentId, studentId);
        return studentGradeService.getMarkReport(studentId);
    }

    @Transactional(readOnly = true)
    public MarkDetailResponse getChildMarkDetail(Long parentId, Long studentId, Long studentGradeId) {
        requireLinkedStudent(parentId, studentId);
        return studentGradeService.getMarkDetail(studentGradeId, studentId, false);
    }

    @Transactional(readOnly = true)
    public List<ScheduleItemResponse> getChildDailySchedule(
            Long parentId,
            Long studentId,
            LocalDate studyDate
    ) {
        requireLinkedStudent(parentId, studentId);
        return scheduleService.findDailySchedule(studentId, studyDate);
    }

    @Transactional(readOnly = true)
    public List<ScheduleItemResponse> getChildWeeklySchedule(
            Long parentId,
            Long studentId,
            LocalDate weekStart
    ) {
        requireLinkedStudent(parentId, studentId);
        return scheduleService.findWeeklySchedule(studentId, weekStart);
    }

    private User requireParent(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        if (!hasRole(user, "PARENT")) {
            throw new BadRequestException("Selected user is not a parent");
        }
        return user;
    }

    private void requireStudent(User user) {
        if (!hasRole(user, "STUDENT")) {
            throw new BadRequestException("Selected user is not a student");
        }
    }

    private User requireStudent(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        requireStudent(user);
        return user;
    }

    private void requireLinkedStudent(Long parentId, Long studentId) {
        requireParent(parentId);
        requireStudent(studentId);
        if (!repository.existsByParentIdAndStudentId(parentId, studentId)) {
            throw new AccessDeniedException("You cannot view an unlinked student's data");
        }
    }

    private boolean hasRole(User user, String roleName) {
        return user.getRoles().stream()
                .anyMatch(role -> roleName.equals(role.getRoleName()));
    }

    private AdminParentResponse mapParent(User user) {
        return new AdminParentResponse(
                user.getId(),
                user.getUserName(),
                fullName(user),
                user.getEmail(),
                user.getPhone(),
                user.getStatus()
        );
    }

    private AdminStudentResponse mapStudent(User user) {
        return new AdminStudentResponse(
                user.getId(),
                user.getUserName(),
                fullName(user),
                user.getEmail(),
                user.getClassName(),
                user.getStatus()
        );
    }

    private String fullName(User user) {
        return java.util.stream.Stream.of(user.getFirstName(), user.getLastName())
                .filter(value -> value != null && !value.isBlank())
                .collect(java.util.stream.Collectors.joining(" "));
    }

    private boolean matchesParent(AdminParentResponse parent, String keyword) {
        if (keyword.isEmpty()) return true;
        return contains(parent.userName(), keyword)
                || contains(parent.fullName(), keyword)
                || contains(parent.email(), keyword)
                || contains(parent.phone(), keyword);
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }
}
