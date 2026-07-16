package com.myfschool.service;

import com.myfschool.dto.response.ScheduleItemResponse;
import com.myfschool.dto.response.TeacherScheduleItemResponse;
import com.myfschool.entity.Schedule;
import com.myfschool.entity.Semester;
import com.myfschool.entity.SemesterSubject;
import com.myfschool.entity.Subject;
import com.myfschool.entity.User;
import com.myfschool.exception.BadRequestException;
import com.myfschool.exception.ResourceNotFoundException;
import com.myfschool.repository.ScheduleRepository;
import com.myfschool.repository.SemesterRepository;
import com.myfschool.repository.SemesterSubjectRepository;
import com.myfschool.repository.StudentSubjectEnrollmentRepository;
import com.myfschool.repository.SubjectRepository;
import com.myfschool.repository.TeacherSubjectRepository;
import com.myfschool.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScheduleService extends AbstractCrudService<Schedule> {

    private final ScheduleRepository repository;
    private final UserRepository userRepository;
    private final SemesterRepository semesterRepository;
    private final SubjectRepository subjectRepository;
    private final SemesterSubjectRepository semesterSubjectRepository;
    private final StudentSubjectEnrollmentRepository enrollmentRepository;
    private final TeacherSubjectRepository teacherSubjectRepository;

    public ScheduleService(
            ScheduleRepository repository,
            UserRepository userRepository,
            SemesterRepository semesterRepository,
            SubjectRepository subjectRepository,
            SemesterSubjectRepository semesterSubjectRepository,
            StudentSubjectEnrollmentRepository enrollmentRepository,
            TeacherSubjectRepository teacherSubjectRepository
    ) {
        super(repository, "Schedule");
        this.repository = repository;
        this.userRepository = userRepository;
        this.semesterRepository = semesterRepository;
        this.subjectRepository = subjectRepository;
        this.semesterSubjectRepository = semesterSubjectRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.teacherSubjectRepository = teacherSubjectRepository;
    }

    @Transactional(readOnly = true)
    public List<Schedule> findByUserId(Long userId) {
        return repository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<Schedule> findByUserIdAndStudyDate(Long userId, LocalDate studyDate) {
        return repository.findByUserIdAndStudyDate(userId, studyDate);
    }

    @Transactional(readOnly = true)
    public List<ScheduleItemResponse> findDailySchedule(Long userId, LocalDate studyDate) {
        return repository.findByUserIdAndStudyDateOrderByStartTimeAsc(userId, studyDate)
                .stream()
                .map(this::toScheduleItem)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ScheduleItemResponse> findWeeklySchedule(Long userId, LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6);
        return repository
                .findByUserIdAndStudyDateBetweenOrderByStudyDateAscStartTimeAsc(
                        userId,
                        weekStart,
                        weekEnd
                )
                .stream()
                .map(this::toScheduleItem)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Schedule> findByUserIdAndSemesterId(Long userId, Long semesterId) {
        return repository.findByUserIdAndSemesterIdOrderByStudyDateAscStartTimeAsc(userId, semesterId);
    }

    @Transactional(readOnly = true)
    public List<Schedule> findByUserIdAndSemesterIdAndStudyDate(
            Long userId,
            Long semesterId,
            LocalDate studyDate
    ) {
        return repository.findByUserIdAndSemesterIdAndStudyDateOrderByStartTimeAsc(
                userId,
                semesterId,
                studyDate
        );
    }

    @Transactional(readOnly = true)
    public List<TeacherScheduleItemResponse> findTeacherDailySchedule(Long teacherId, LocalDate studyDate) {
        requireTeacher(teacherId);
        return toTeacherScheduleItems(repository.findByTeacherIdAndStudyDateOrderByStartTimeAsc(
                teacherId,
                studyDate
        ));
    }

    @Transactional(readOnly = true)
    public List<TeacherScheduleItemResponse> findTeacherWeeklySchedule(Long teacherId, LocalDate weekStart) {
        requireTeacher(teacherId);
        return toTeacherScheduleItems(repository.findByTeacherIdAndStudyDateBetweenOrderByStudyDateAscStartTimeAsc(
                teacherId,
                weekStart,
                weekStart.plusDays(6)
        ));
    }

    @Transactional(readOnly = true)
    public List<TeacherScheduleItemResponse> findTeacherSchedule(
            Long teacherId,
            Long semesterId,
            LocalDate studyDate
    ) {
        requireTeacher(teacherId);
        if (semesterId != null && studyDate != null) {
            return toTeacherScheduleItems(repository.findByTeacherIdAndSemesterIdAndStudyDateOrderByStartTimeAsc(
                    teacherId,
                    semesterId,
                    studyDate
            ));
        }
        if (semesterId != null) {
            return toTeacherScheduleItems(repository.findByTeacherIdAndSemesterIdOrderByStudyDateAscStartTimeAsc(
                    teacherId,
                    semesterId
            ));
        }
        if (studyDate != null) {
            return findTeacherDailySchedule(teacherId, studyDate);
        }
        return toTeacherScheduleItems(repository.findByTeacherIdOrderByStudyDateAscStartTimeAsc(teacherId));
    }

    @Override
    @Transactional
    public Schedule create(Schedule schedule) {
        validateSchedule(schedule, null);
        schedule.setId(null);
        return repository.save(schedule);
    }

    @Override
    @Transactional
    public Schedule update(Long id, Schedule schedule) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Schedule", id);
        }
        validateSchedule(schedule, id);
        schedule.setId(id);
        return repository.save(schedule);
    }

    private void validateSchedule(Schedule schedule, Long currentId) {
        User student = userRepository.findById(schedule.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", schedule.getUserId()));
        if (!hasRole(student, "STUDENT")) {
            throw new BadRequestException("Người dùng được chọn không phải sinh viên");
        }

        semesterRepository.findById(schedule.getSemesterId())
                .orElseThrow(() -> new ResourceNotFoundException("Semester", schedule.getSemesterId()));
        subjectRepository.findById(schedule.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", schedule.getSubjectId()));
        if (schedule.getTeacherId() != null) {
            User teacher = requireTeacher(schedule.getTeacherId());
            if (!teacherSubjectRepository.existsByTeacherIdAndSubjectId(teacher.getId(), schedule.getSubjectId())) {
                throw new BadRequestException("Giáo viên chưa được gán môn học này");
            }
            if (schedule.getLecturerName() == null || schedule.getLecturerName().isBlank()) {
                schedule.setLecturerName(fullName(teacher));
            }
        }

        SemesterSubject offering = semesterSubjectRepository
                .findBySemesterIdAndSubjectIdOrderByStartDateAscIdAsc(
                        schedule.getSemesterId(),
                        schedule.getSubjectId()
                )
                .stream()
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Môn học không được mở trong học kỳ đã chọn"));
        if (!enrollmentRepository.existsByUserIdAndSemesterIdAndSubjectId(
                schedule.getUserId(), schedule.getSemesterId(), schedule.getSubjectId()
        )) {
            throw new BadRequestException("Sinh viên chưa được gán môn học này trong học kỳ đã chọn");
        }
        if (schedule.getStudyDate().isBefore(offering.getStartDate())
                || schedule.getStudyDate().isAfter(offering.getEndDate())) {
            throw new BadRequestException("Ngày học phải nằm trong thời gian mở môn");
        }
        if (!schedule.getEndTime().isAfter(schedule.getStartTime())) {
            throw new BadRequestException("Giờ kết thúc phải sau giờ bắt đầu");
        }

        boolean studentOverlaps = repository
                .findByUserIdAndStudyDateOrderByStartTimeAsc(schedule.getUserId(), schedule.getStudyDate())
                .stream()
                .filter(existing -> currentId == null || !existing.getId().equals(currentId))
                .anyMatch(existing -> schedule.getStartTime().isBefore(existing.getEndTime())
                        && schedule.getEndTime().isAfter(existing.getStartTime()));
        if (studentOverlaps) {
            throw new BadRequestException("Sinh viên đã có lịch học trùng thời gian");
        }
        if (schedule.getTeacherId() != null && hasTeacherOverlap(schedule, currentId)) {
            throw new BadRequestException("Giáo viên đã có lịch dạy trùng thời gian");
        }
    }

    private ScheduleItemResponse toScheduleItem(Schedule schedule) {
        Subject subject = subjectRepository.findById(schedule.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", schedule.getSubjectId()));
        Semester semester = semesterRepository.findById(schedule.getSemesterId())
                .orElseThrow(() -> new ResourceNotFoundException("Semester", schedule.getSemesterId()));
        return new ScheduleItemResponse(
                schedule.getId(),
                semester.getId(),
                semester.getName(),
                subject.getId(),
                subject.getSubjectCode(),
                subject.getSubjectName(),
                schedule.getStudyDate(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getRoom(),
                schedule.getLecturerName(),
                schedule.getNote()
        );
    }

    private List<TeacherScheduleItemResponse> toTeacherScheduleItems(List<Schedule> schedules) {
        Map<TeacherScheduleKey, List<Schedule>> grouped = new LinkedHashMap<>();
        schedules.forEach(schedule -> grouped
                .computeIfAbsent(TeacherScheduleKey.from(schedule), ignored -> new java.util.ArrayList<>())
                .add(schedule));
        return grouped.values().stream()
                .map(this::toTeacherScheduleItem)
                .toList();
    }

    private TeacherScheduleItemResponse toTeacherScheduleItem(List<Schedule> schedules) {
        Schedule first = schedules.get(0);
        Subject subject = subjectRepository.findById(first.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", first.getSubjectId()));
        Semester semester = semesterRepository.findById(first.getSemesterId())
                .orElseThrow(() -> new ResourceNotFoundException("Semester", first.getSemesterId()));
        Set<Long> studentIds = schedules.stream()
                .map(Schedule::getUserId)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        List<String> classNames = userRepository.findAllById(studentIds).stream()
                .map(User::getClassName)
                .filter(className -> className != null && !className.isBlank())
                .distinct()
                .sorted()
                .toList();

        return new TeacherScheduleItemResponse(
                first.getId(),
                semester.getId(),
                semester.getName(),
                subject.getId(),
                subject.getSubjectCode(),
                subject.getSubjectName(),
                first.getStudyDate(),
                first.getStartTime(),
                first.getEndTime(),
                first.getRoom(),
                first.getLecturerName(),
                first.getNote(),
                studentIds.size(),
                classNames
        );
    }

    private boolean hasTeacherOverlap(Schedule schedule, Long currentId) {
        return repository
                .findByTeacherIdAndStudyDateOrderByStartTimeAsc(schedule.getTeacherId(), schedule.getStudyDate())
                .stream()
                .filter(existing -> currentId == null || !existing.getId().equals(currentId))
                .filter(existing -> schedule.getStartTime().isBefore(existing.getEndTime())
                        && schedule.getEndTime().isAfter(existing.getStartTime()))
                .anyMatch(existing -> !isSameTeachingSlot(schedule, existing));
    }

    private boolean isSameTeachingSlot(Schedule schedule, Schedule existing) {
        return Objects.equals(schedule.getSemesterId(), existing.getSemesterId())
                && Objects.equals(schedule.getSubjectId(), existing.getSubjectId())
                && Objects.equals(schedule.getStudyDate(), existing.getStudyDate())
                && Objects.equals(schedule.getStartTime(), existing.getStartTime())
                && Objects.equals(schedule.getEndTime(), existing.getEndTime())
                && Objects.equals(schedule.getRoom(), existing.getRoom());
    }

    private User requireTeacher(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        if (!hasRole(user, "SUBJECT_TEACHER") && !hasRole(user, "TEACHER")) {
            throw new BadRequestException("Selected user is not a subject teacher");
        }
        return user;
    }

    private boolean hasRole(User user, String roleName) {
        return user.getRoles().stream()
                .anyMatch(role -> roleName.equals(role.getRoleName()));
    }

    private String fullName(User user) {
        return java.util.stream.Stream.of(user.getFirstName(), user.getLastName())
                .filter(value -> value != null && !value.isBlank())
                .collect(java.util.stream.Collectors.joining(" "));
    }

    private record TeacherScheduleKey(
            Long semesterId,
            Long subjectId,
            LocalDate studyDate,
            LocalTime startTime,
            LocalTime endTime,
            String room,
            String lecturerName,
            String note
    ) {

        private static TeacherScheduleKey from(Schedule schedule) {
            return new TeacherScheduleKey(
                    schedule.getSemesterId(),
                    schedule.getSubjectId(),
                    schedule.getStudyDate(),
                    schedule.getStartTime(),
                    schedule.getEndTime(),
                    schedule.getRoom(),
                    schedule.getLecturerName(),
                    schedule.getNote()
            );
        }
    }
}
