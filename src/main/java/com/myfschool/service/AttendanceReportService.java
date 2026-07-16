package com.myfschool.service;

import com.myfschool.dto.response.AttendanceReportItemResponse;
import com.myfschool.dto.response.AttendanceReportSemesterResponse;
import com.myfschool.entity.AttendanceRecord;
import com.myfschool.entity.Semester;
import com.myfschool.entity.SemesterSubject;
import com.myfschool.entity.Subject;
import com.myfschool.entity.User;
import com.myfschool.exception.ResourceNotFoundException;
import com.myfschool.repository.AttendanceRecordRepository;
import com.myfschool.repository.SemesterRepository;
import com.myfschool.repository.SemesterSubjectRepository;
import com.myfschool.repository.SubjectRepository;
import com.myfschool.repository.UserRepository;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttendanceReportService {

    private final AttendanceRecordRepository repository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final SemesterRepository semesterRepository;
    private final SemesterSubjectRepository semesterSubjectRepository;

    public AttendanceReportService(
            AttendanceRecordRepository repository,
            UserRepository userRepository,
            SubjectRepository subjectRepository,
            SemesterRepository semesterRepository,
            SemesterSubjectRepository semesterSubjectRepository
    ) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
        this.semesterRepository = semesterRepository;
        this.semesterSubjectRepository = semesterSubjectRepository;
    }

    @Transactional(readOnly = true)
    public List<AttendanceReportSemesterResponse> getReport(Long userId) {
        User student = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        Map<Long, Subject> subjectsById = subjectRepository.findAll().stream()
                .collect(Collectors.toMap(Subject::getId, Function.identity()));
        Map<SemesterSubjectKey, SemesterSubject> offeringsByKey = semesterSubjectRepository.findAll().stream()
                .collect(Collectors.toMap(
                        SemesterSubjectKey::from,
                        Function.identity(),
                        (first, ignored) -> first
                ));
        Map<Long, List<AttendanceRecord>> recordsBySemester = repository
                .findByUserIdOrderBySemesterIdAscSubjectIdAscAttendanceDateAsc(userId)
                .stream()
                .collect(Collectors.groupingBy(AttendanceRecord::getSemesterId));

        return semesterRepository.findAll().stream()
                .sorted(attendanceSemesterComparator())
                .map(semester -> new AttendanceReportSemesterResponse(
                        semester.getId(),
                        semester.getName(),
                        semester.getSchoolYear(),
                        semester.getStartDate(),
                        semester.getEndDate(),
                        mapSemesterReports(
                                recordsBySemester.getOrDefault(semester.getId(), List.of()),
                                subjectsById,
                                offeringsByKey,
                                student.getClassName(),
                                semester
                        )
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AttendanceReportItemResponse> getSemesterReport(Long userId, Long semesterId) {
        User student = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new ResourceNotFoundException("Semester", semesterId));
        Map<Long, Subject> subjectsById = subjectRepository.findAll().stream()
                .collect(Collectors.toMap(Subject::getId, Function.identity()));
        Map<SemesterSubjectKey, SemesterSubject> offeringsByKey = semesterSubjectRepository.findAll().stream()
                .collect(Collectors.toMap(
                        SemesterSubjectKey::from,
                        Function.identity(),
                        (first, ignored) -> first
                ));
        return mapSemesterReports(
                repository.findByUserIdAndSemesterIdOrderBySubjectIdAscAttendanceDateAsc(userId, semesterId),
                subjectsById,
                offeringsByKey,
                student.getClassName(),
                semester
        );
    }

    private List<AttendanceReportItemResponse> mapSemesterReports(
            List<AttendanceRecord> records,
            Map<Long, Subject> subjectsById,
            Map<SemesterSubjectKey, SemesterSubject> offeringsByKey,
            String className,
            Semester semester
    ) {
        return records.stream()
                .collect(Collectors.groupingBy(AttendanceRecord::getSubjectId))
                .entrySet()
                .stream()
                .map(entry -> mapSubjectReport(
                        entry.getKey(),
                        entry.getValue(),
                        subjectsById.get(entry.getKey()),
                        offeringsByKey.get(new SemesterSubjectKey(semester.getId(), entry.getKey())),
                        className,
                        semester
                ))
                .sorted(Comparator.comparing(AttendanceReportItemResponse::subjectCode))
                .toList();
    }

    private AttendanceReportItemResponse mapSubjectReport(
            Long subjectId,
            List<AttendanceRecord> records,
            Subject subject,
            SemesterSubject offering,
            String className,
            Semester semester
    ) {
        long totalSessions = records.size();
        long attendedSessions = records.stream()
                .filter(record -> isAttended(record.getStatus()))
                .count();
        long absentSessions = Math.max(totalSessions - attendedSessions, 0);
        int percentage = totalSessions == 0
                ? 0
                : Math.toIntExact(Math.round(attendedSessions * 100.0 / totalSessions));
        LocalDate startDate = offering == null ? semester.getStartDate() : offering.getStartDate();
        LocalDate endDate = offering == null ? semester.getEndDate() : offering.getEndDate();

        return new AttendanceReportItemResponse(
                subjectId,
                subject == null ? "" : subject.getSubjectCode(),
                subject == null ? "Unknown subject" : subject.getSubjectName(),
                className,
                startDate,
                endDate,
                attendedSessions,
                totalSessions,
                absentSessions,
                percentage
        );
    }

    private boolean isAttended(String status) {
        return Objects.equals(status, "PRESENT")
                || Objects.equals(status, "LATE")
                || Objects.equals(status, "EXCUSED");
    }

    private Comparator<Semester> attendanceSemesterComparator() {
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

    private record SemesterSubjectKey(Long semesterId, Long subjectId) {

        private static SemesterSubjectKey from(SemesterSubject semesterSubject) {
            return new SemesterSubjectKey(
                    semesterSubject.getSemesterId(),
                    semesterSubject.getSubjectId()
            );
        }
    }
}
