package com.myfschool.service;

import com.myfschool.dto.response.MarkDetailItemResponse;
import com.myfschool.dto.response.MarkDetailResponse;
import com.myfschool.dto.response.MarkReportGradeResponse;
import com.myfschool.dto.response.MarkReportSemesterResponse;
import com.myfschool.entity.Semester;
import com.myfschool.entity.StudentGrade;
import com.myfschool.entity.StudentGradeItem;
import com.myfschool.entity.Subject;
import com.myfschool.entity.User;
import com.myfschool.exception.ResourceNotFoundException;
import com.myfschool.repository.SemesterRepository;
import com.myfschool.repository.StudentGradeItemRepository;
import com.myfschool.repository.StudentGradeRepository;
import com.myfschool.repository.SubjectRepository;
import com.myfschool.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentGradeService extends AbstractCrudService<StudentGrade> {

    private final StudentGradeRepository repository;
    private final StudentGradeItemRepository itemRepository;
    private final SubjectRepository subjectRepository;
    private final SemesterRepository semesterRepository;
    private final UserRepository userRepository;

    public StudentGradeService(
            StudentGradeRepository repository,
            StudentGradeItemRepository itemRepository,
            SubjectRepository subjectRepository,
            SemesterRepository semesterRepository,
            UserRepository userRepository
    ) {
        super(repository, "Student grade");
        this.repository = repository;
        this.itemRepository = itemRepository;
        this.subjectRepository = subjectRepository;
        this.semesterRepository = semesterRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<StudentGrade> findByUserId(Long userId) {
        return repository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<StudentGrade> findByUserIdAndSemesterId(Long userId, Long semesterId) {
        return repository.findByUserIdAndSemesterId(userId, semesterId);
    }

    @Transactional(readOnly = true)
    public List<MarkReportSemesterResponse> getMarkReport(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        String className = user.getClassName();

        Map<Long, Subject> subjectsById = subjectRepository.findAll().stream()
                .collect(Collectors.toMap(Subject::getId, Function.identity()));
        List<StudentGrade> grades = repository.findByUserId(userId);
        Map<Long, List<MarkReportGradeResponse>> gradesBySemester = grades.stream()
                .collect(Collectors.groupingBy(
                        StudentGrade::getSemesterId,
                        Collectors.mapping(
                                grade -> mapToMarkReportGrade(grade, subjectsById.get(grade.getSubjectId()), className),
                                Collectors.toList()
                        )
                ));

        return semesterRepository.findAll().stream()
                .sorted(markReportSemesterComparator())
                .map(semester -> new MarkReportSemesterResponse(
                        semester.getId(),
                        semester.getName(),
                        semester.getSchoolYear(),
                        semester.getStartDate(),
                        semester.getEndDate(),
                        gradesBySemester.getOrDefault(semester.getId(), List.of())
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public MarkDetailResponse getMarkDetail(Long studentGradeId) {
        StudentGrade grade = repository.findById(studentGradeId)
                .orElseThrow(() -> new ResourceNotFoundException("Student grade", studentGradeId));
        User user = userRepository.findById(grade.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", grade.getUserId()));
        Subject subject = subjectRepository.findById(grade.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", grade.getSubjectId()));

        BigDecimal average = grade.getTotalScore() == null ? BigDecimal.ZERO : grade.getTotalScore();
        List<MarkDetailItemResponse> items = itemRepository
                .findByStudentGradeIdOrderByDisplayOrderAscIdAsc(studentGradeId)
                .stream()
                .map(this::mapToMarkDetailItem)
                .toList();

        return new MarkDetailResponse(
                grade.getId(),
                grade.getSubjectId(),
                subject.getSubjectCode(),
                subject.getSubjectName(),
                user.getClassName(),
                average,
                grade.getLetterGrade(),
                average.compareTo(BigDecimal.valueOf(5)) >= 0,
                items
        );
    }

    private MarkReportGradeResponse mapToMarkReportGrade(StudentGrade grade, Subject subject, String className) {
        BigDecimal average = grade.getTotalScore() == null ? BigDecimal.ZERO : grade.getTotalScore();
        return new MarkReportGradeResponse(
                grade.getId(),
                grade.getSubjectId(),
                subject == null ? "" : subject.getSubjectCode(),
                subject == null ? "Unknown subject" : subject.getSubjectName(),
                className,
                average,
                grade.getLetterGrade(),
                average.compareTo(BigDecimal.valueOf(5)) >= 0
        );
    }

    private MarkDetailItemResponse mapToMarkDetailItem(StudentGradeItem item) {
        return new MarkDetailItemResponse(
                item.getId(),
                item.getGradeCategory(),
                item.getGradeItem(),
                item.getWeight(),
                item.getValue()
        );
    }

    private Comparator<Semester> markReportSemesterComparator() {
        LocalDate today = LocalDate.now();
        return Comparator
                .comparingInt((Semester semester) -> semesterBucket(semester, today))
                .thenComparing((Semester semester) -> semester.getStartDate() == null)
                .thenComparing(Semester::getStartDate, Comparator.nullsLast(Comparator.reverseOrder()));
    }

    private int semesterBucket(Semester semester, LocalDate today) {
        if (isCurrentSemester(semester, today)) {
            return 0;
        }
        if (semester.getStartDate() != null && semester.getStartDate().isAfter(today)) {
            return 2;
        }
        return 1;
    }

    private boolean isCurrentSemester(Semester semester, LocalDate today) {
        if (semester.getStartDate() == null || semester.getEndDate() == null) {
            return false;
        }
        return !today.isBefore(semester.getStartDate()) && !today.isAfter(semester.getEndDate());
    }
}
