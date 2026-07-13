package com.myfschool.service;

import com.myfschool.entity.Schedule;
import com.myfschool.entity.SemesterSubject;
import com.myfschool.entity.User;
import com.myfschool.exception.BadRequestException;
import com.myfschool.exception.ResourceNotFoundException;
import com.myfschool.repository.ScheduleRepository;
import com.myfschool.repository.SemesterRepository;
import com.myfschool.repository.SemesterSubjectRepository;
import com.myfschool.repository.StudentSubjectEnrollmentRepository;
import com.myfschool.repository.SubjectRepository;
import com.myfschool.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
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

    public ScheduleService(
            ScheduleRepository repository,
            UserRepository userRepository,
            SemesterRepository semesterRepository,
            SubjectRepository subjectRepository,
            SemesterSubjectRepository semesterSubjectRepository,
            StudentSubjectEnrollmentRepository enrollmentRepository
    ) {
        super(repository, "Schedule");
        this.repository = repository;
        this.userRepository = userRepository;
        this.semesterRepository = semesterRepository;
        this.subjectRepository = subjectRepository;
        this.semesterSubjectRepository = semesterSubjectRepository;
        this.enrollmentRepository = enrollmentRepository;
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
        boolean isStudent = student.getRoles().stream()
                .anyMatch(role -> "STUDENT".equals(role.getRoleName()));
        if (!isStudent) {
            throw new BadRequestException("Người dùng được chọn không phải sinh viên");
        }

        semesterRepository.findById(schedule.getSemesterId())
                .orElseThrow(() -> new ResourceNotFoundException("Semester", schedule.getSemesterId()));
        subjectRepository.findById(schedule.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", schedule.getSubjectId()));

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

        boolean overlaps = repository
                .findByUserIdAndStudyDateOrderByStartTimeAsc(schedule.getUserId(), schedule.getStudyDate())
                .stream()
                .filter(existing -> currentId == null || !existing.getId().equals(currentId))
                .anyMatch(existing -> schedule.getStartTime().isBefore(existing.getEndTime())
                        && schedule.getEndTime().isAfter(existing.getStartTime()));
        if (overlaps) {
            throw new BadRequestException("Sinh viên đã có lịch học trùng thời gian");
        }
    }
}
