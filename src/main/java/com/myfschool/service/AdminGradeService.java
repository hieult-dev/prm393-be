package com.myfschool.service;

import com.myfschool.dto.request.GradeItemInput;
import com.myfschool.dto.request.SaveGradeRequest;
import com.myfschool.dto.response.AdminGradeItemResponse;
import com.myfschool.dto.response.AdminGradeResponse;
import com.myfschool.dto.response.AdminStudentResponse;
import com.myfschool.entity.Semester;
import com.myfschool.entity.StudentGrade;
import com.myfschool.entity.StudentGradeItem;
import com.myfschool.entity.StudentSubjectEnrollment;
import com.myfschool.entity.Subject;
import com.myfschool.entity.User;
import com.myfschool.exception.BadRequestException;
import com.myfschool.exception.ResourceAlreadyExistsException;
import com.myfschool.exception.ResourceNotFoundException;
import com.myfschool.repository.SemesterRepository;
import com.myfschool.repository.SemesterSubjectRepository;
import com.myfschool.repository.StudentGradeItemRepository;
import com.myfschool.repository.StudentGradeRepository;
import com.myfschool.repository.StudentSubjectEnrollmentRepository;
import com.myfschool.repository.SubjectRepository;
import com.myfschool.repository.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminGradeService {

    private static final BigDecimal TOTAL_WEIGHT = new BigDecimal("100.00");

    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final SemesterRepository semesterRepository;
    private final SemesterSubjectRepository semesterSubjectRepository;
    private final StudentGradeRepository gradeRepository;
    private final StudentGradeItemRepository itemRepository;
    private final StudentSubjectEnrollmentRepository enrollmentRepository;

    public AdminGradeService(
            UserRepository userRepository,
            SubjectRepository subjectRepository,
            SemesterRepository semesterRepository,
            SemesterSubjectRepository semesterSubjectRepository,
            StudentGradeRepository gradeRepository,
            StudentGradeItemRepository itemRepository,
            StudentSubjectEnrollmentRepository enrollmentRepository
    ) {
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
        this.semesterRepository = semesterRepository;
        this.semesterSubjectRepository = semesterSubjectRepository;
        this.gradeRepository = gradeRepository;
        this.itemRepository = itemRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminStudentResponse> getStudents(String search) {
        String keyword = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        return userRepository.findDistinctByRolesRoleNameOrderByUserNameAsc("STUDENT").stream()
                .map(this::mapStudent)
                .filter(student -> matchesStudent(student, keyword))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Subject> getSubjects(Long semesterId) {
        java.util.Set<Long> offeredSubjectIds = semesterId == null
                ? null
                : semesterSubjectRepository.findBySemesterIdOrderByStartDateAscIdAsc(semesterId)
                        .stream()
                        .map(com.myfschool.entity.SemesterSubject::getSubjectId)
                        .collect(java.util.stream.Collectors.toSet());
        return subjectRepository.findAll().stream()
                .filter(subject -> offeredSubjectIds == null || offeredSubjectIds.contains(subject.getId()))
                .sorted(Comparator.comparing(Subject::getSubjectCode))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Semester> getSemesters() {
        return semesterRepository.findAll().stream()
                .sorted(Comparator.comparing(
                        Semester::getStartDate,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Subject> getAssignedSubjects(Long userId, Long semesterId) {
        requireStudent(userId);
        requireSemester(semesterId);
        Set<Long> assignedSubjectIds = enrollmentRepository
                .findByUserIdAndSemesterIdOrderByIdAsc(userId, semesterId)
                .stream()
                .map(StudentSubjectEnrollment::getSubjectId)
                .collect(java.util.stream.Collectors.toSet());
        return subjectRepository.findAll().stream()
                .filter(subject -> assignedSubjectIds.contains(subject.getId()))
                .sorted(Comparator.comparing(Subject::getSubjectCode))
                .toList();
    }

    @Transactional
    public List<Subject> assignSubjects(Long userId, Long semesterId, List<Long> subjectIds) {
        requireStudent(userId);
        requireSemester(semesterId);

        Set<Long> selectedSubjectIds = new HashSet<>(subjectIds);
        Set<Long> offeredSubjectIds = semesterSubjectRepository
                .findBySemesterIdOrderByStartDateAscIdAsc(semesterId)
                .stream()
                .map(com.myfschool.entity.SemesterSubject::getSubjectId)
                .collect(java.util.stream.Collectors.toSet());
        if (!offeredSubjectIds.containsAll(selectedSubjectIds)) {
            throw new BadRequestException("Có môn học không được mở trong học kỳ đã chọn");
        }

        Set<Long> gradedSubjectIds = gradeRepository.findByUserIdAndSemesterId(userId, semesterId)
                .stream()
                .map(StudentGrade::getSubjectId)
                .collect(java.util.stream.Collectors.toSet());
        if (!selectedSubjectIds.containsAll(gradedSubjectIds)) {
            throw new BadRequestException("Không thể bỏ gán môn học đã có điểm");
        }

        List<StudentSubjectEnrollment> current = enrollmentRepository
                .findByUserIdAndSemesterIdOrderByIdAsc(userId, semesterId);
        enrollmentRepository.deleteAll(current.stream()
                .filter(enrollment -> !selectedSubjectIds.contains(enrollment.getSubjectId()))
                .toList());

        Set<Long> currentSubjectIds = current.stream()
                .map(StudentSubjectEnrollment::getSubjectId)
                .collect(java.util.stream.Collectors.toSet());
        selectedSubjectIds.stream()
                .filter(subjectId -> !currentSubjectIds.contains(subjectId))
                .forEach(subjectId -> {
                    StudentSubjectEnrollment enrollment = new StudentSubjectEnrollment();
                    enrollment.setUserId(userId);
                    enrollment.setSemesterId(semesterId);
                    enrollment.setSubjectId(subjectId);
                    enrollmentRepository.save(enrollment);
                });

        return getAssignedSubjects(userId, semesterId);
    }

    @Transactional(readOnly = true)
    public List<AdminGradeResponse> getGrades(Long userId, Long semesterId) {
        List<StudentGrade> grades;
        if (userId != null && semesterId != null) {
            grades = gradeRepository.findByUserIdAndSemesterId(userId, semesterId);
        } else if (userId != null) {
            grades = gradeRepository.findByUserId(userId);
        } else {
            grades = gradeRepository.findAll();
        }

        return grades.stream()
                .filter(grade -> semesterId == null || semesterId.equals(grade.getSemesterId()))
                .map(this::mapGrade)
                .sorted(Comparator.comparing(AdminGradeResponse::subjectCode))
                .toList();
    }

    @Transactional
    public AdminGradeResponse createGrade(SaveGradeRequest request) {
        gradeRepository.findByUserIdAndSubjectIdAndSemesterId(
                request.userId(), request.subjectId(), request.semesterId()
        ).ifPresent(existing -> {
            throw new ResourceAlreadyExistsException(
                    "Sinh viên đã có điểm cho môn học trong học kỳ này"
            );
        });

        StudentGrade grade = new StudentGrade();
        return saveGrade(grade, request);
    }

    @Transactional
    public AdminGradeResponse updateGrade(Long id, SaveGradeRequest request) {
        StudentGrade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student grade", id));
        gradeRepository.findByUserIdAndSubjectIdAndSemesterId(
                request.userId(), request.subjectId(), request.semesterId()
        ).filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ResourceAlreadyExistsException(
                            "Sinh viên đã có điểm cho môn học trong học kỳ này"
                    );
                });
        return saveGrade(grade, request);
    }

    @Transactional
    public void deleteGrade(Long id) {
        if (!gradeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Student grade", id);
        }
        gradeRepository.deleteById(id);
    }

    private AdminGradeResponse saveGrade(StudentGrade grade, SaveGradeRequest request) {
        User student = requireStudent(request.userId());
        requireSubject(request.subjectId());
        requireSemester(request.semesterId());
        if (!semesterSubjectRepository.existsBySemesterIdAndSubjectId(
                request.semesterId(), request.subjectId()
        )) {
            throw new BadRequestException("Môn học không được mở trong học kỳ đã chọn");
        }
        if (!enrollmentRepository.existsByUserIdAndSemesterIdAndSubjectId(
                request.userId(), request.semesterId(), request.subjectId()
        )) {
            throw new BadRequestException("Sinh viên chưa được gán môn học này");
        }
        validateItems(request.items());

        BigDecimal totalScore = calculateTotal(request.items());
        grade.setUserId(student.getId());
        grade.setSubjectId(request.subjectId());
        grade.setSemesterId(request.semesterId());
        grade.setTotalScore(totalScore);
        grade.setLetterGrade(toLetterGrade(totalScore));
        StudentGrade saved = gradeRepository.save(grade);

        itemRepository.deleteByStudentGradeId(saved.getId());
        for (int index = 0; index < request.items().size(); index++) {
            GradeItemInput input = request.items().get(index);
            StudentGradeItem item = new StudentGradeItem();
            item.setStudentGradeId(saved.getId());
            item.setGradeCategory(input.name().trim());
            item.setGradeItem(input.name().trim());
            item.setWeight(input.weight().setScale(2, RoundingMode.HALF_UP));
            item.setValue(input.score().stripTrailingZeros().toPlainString());
            item.setDisplayOrder((index + 1) * 10);
            itemRepository.save(item);
        }

        return mapGrade(saved);
    }

    private void validateItems(List<GradeItemInput> items) {
        BigDecimal weight = items.stream()
                .map(GradeItemInput::weight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (weight.compareTo(TOTAL_WEIGHT) != 0) {
            throw new BadRequestException("Tổng trọng số các đầu điểm phải bằng 100%");
        }
    }

    private BigDecimal calculateTotal(List<GradeItemInput> items) {
        return items.stream()
                .map(item -> item.score().multiply(item.weight()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(TOTAL_WEIGHT, 2, RoundingMode.HALF_UP);
    }

    private String toLetterGrade(BigDecimal score) {
        if (score.compareTo(new BigDecimal("8.50")) >= 0) return "A";
        if (score.compareTo(new BigDecimal("8.00")) >= 0) return "B+";
        if (score.compareTo(new BigDecimal("7.00")) >= 0) return "B";
        if (score.compareTo(new BigDecimal("6.50")) >= 0) return "C+";
        if (score.compareTo(new BigDecimal("5.00")) >= 0) return "C";
        return "F";
    }

    private User requireStudent(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        boolean isStudent = user.getRoles().stream()
                .anyMatch(role -> "STUDENT".equals(role.getRoleName()));
        if (!isStudent) {
            throw new BadRequestException("Người dùng được chọn không phải sinh viên");
        }
        return user;
    }

    private Subject requireSubject(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", id));
    }

    private Semester requireSemester(Long id) {
        return semesterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Semester", id));
    }

    private AdminStudentResponse mapStudent(User user) {
        String fullName = java.util.stream.Stream.of(user.getFirstName(), user.getLastName())
                .filter(value -> value != null && !value.isBlank())
                .collect(java.util.stream.Collectors.joining(" "));
        return new AdminStudentResponse(
                user.getId(),
                user.getUserName(),
                fullName,
                user.getEmail(),
                user.getClassName(),
                user.getStatus()
        );
    }

    private boolean matchesStudent(AdminStudentResponse student, String keyword) {
        if (keyword.isEmpty()) return true;
        return contains(student.userName(), keyword)
                || contains(student.fullName(), keyword)
                || contains(student.email(), keyword)
                || contains(student.className(), keyword);
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private AdminGradeResponse mapGrade(StudentGrade grade) {
        User student = userRepository.findById(grade.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", grade.getUserId()));
        Subject subject = requireSubject(grade.getSubjectId());
        Semester semester = requireSemester(grade.getSemesterId());
        List<AdminGradeItemResponse> items = itemRepository
                .findByStudentGradeIdOrderByDisplayOrderAscIdAsc(grade.getId())
                .stream()
                .map(item -> new AdminGradeItemResponse(
                        item.getId(),
                        item.getGradeItem(),
                        item.getWeight(),
                        parseScore(item.getValue())
                ))
                .toList();
        AdminStudentResponse studentResponse = mapStudent(student);

        return new AdminGradeResponse(
                grade.getId(),
                student.getId(),
                student.getUserName(),
                studentResponse.fullName(),
                student.getClassName(),
                subject.getId(),
                subject.getSubjectCode(),
                subject.getSubjectName(),
                semester.getId(),
                semester.getName(),
                grade.getTotalScore(),
                grade.getLetterGrade(),
                items
        );
    }

    private BigDecimal parseScore(String value) {
        try {
            return value == null ? BigDecimal.ZERO : new BigDecimal(value);
        } catch (NumberFormatException ignored) {
            return BigDecimal.ZERO;
        }
    }
}
