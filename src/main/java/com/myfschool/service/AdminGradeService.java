package com.myfschool.service;

import com.myfschool.dto.request.GradeItemInput;
import com.myfschool.dto.request.SaveGradeRequest;
import com.myfschool.dto.response.AdminGradeItemResponse;
import com.myfschool.dto.response.AdminGradeResponse;
import com.myfschool.dto.response.AdminStudentResponse;
import com.myfschool.dto.response.AdminTeacherResponse;
import com.myfschool.dto.response.GradeImportResultResponse;
import com.myfschool.entity.Semester;
import com.myfschool.entity.StudentGrade;
import com.myfschool.entity.StudentGradeItem;
import com.myfschool.entity.StudentSubjectEnrollment;
import com.myfschool.entity.Subject;
import com.myfschool.entity.TeacherSubject;
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
import com.myfschool.repository.TeacherSubjectRepository;
import com.myfschool.repository.UserRepository;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AdminGradeService {

    private static final BigDecimal TOTAL_WEIGHT = new BigDecimal("100.00");
    private static final int GRADE_START_COLUMN = 4;
    private static final List<GradeComponent> DEFAULT_GRADE_COMPONENTS = List.of(
            new GradeComponent("PT1", new BigDecimal("50.00")),
            new GradeComponent("PT2", new BigDecimal("50.00"))
    );

    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final SemesterRepository semesterRepository;
    private final SemesterSubjectRepository semesterSubjectRepository;
    private final StudentGradeRepository gradeRepository;
    private final StudentGradeItemRepository itemRepository;
    private final StudentSubjectEnrollmentRepository enrollmentRepository;
    private final TeacherSubjectRepository teacherSubjectRepository;

    public AdminGradeService(
            UserRepository userRepository,
            SubjectRepository subjectRepository,
            SemesterRepository semesterRepository,
            SemesterSubjectRepository semesterSubjectRepository,
            StudentGradeRepository gradeRepository,
            StudentGradeItemRepository itemRepository,
            StudentSubjectEnrollmentRepository enrollmentRepository,
            TeacherSubjectRepository teacherSubjectRepository
    ) {
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
        this.semesterRepository = semesterRepository;
        this.semesterSubjectRepository = semesterSubjectRepository;
        this.gradeRepository = gradeRepository;
        this.itemRepository = itemRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.teacherSubjectRepository = teacherSubjectRepository;
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
    public List<AdminTeacherResponse> getTeachers(String search) {
        String keyword = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        return userRepository.findDistinctByRolesRoleNameOrderByUserNameAsc("SUBJECT_TEACHER").stream()
                .map(this::mapTeacher)
                .filter(teacher -> matchesTeacher(teacher, keyword))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Subject> getTeacherSubjects(Long teacherId) {
        requireTeacher(teacherId);
        Set<Long> assignedSubjectIds = getTeacherSubjectIds(teacherId);
        return subjectRepository.findAll().stream()
                .filter(subject -> assignedSubjectIds.contains(subject.getId()))
                .sorted(Comparator.comparing(Subject::getSubjectCode))
                .toList();
    }

    @Transactional
    public List<Subject> assignTeacherSubjects(Long teacherId, List<Long> subjectIds) {
        requireTeacher(teacherId);

        Set<Long> selectedSubjectIds = new HashSet<>(subjectIds);
        Set<Long> existingSubjectIds = subjectRepository.findAllById(selectedSubjectIds)
                .stream()
                .map(Subject::getId)
                .collect(java.util.stream.Collectors.toSet());
        if (!existingSubjectIds.containsAll(selectedSubjectIds)) {
            throw new BadRequestException("Subject not found in selected teacher subjects");
        }

        List<TeacherSubject> current = teacherSubjectRepository.findByTeacherIdOrderByIdAsc(teacherId);
        teacherSubjectRepository.deleteAll(current.stream()
                .filter(assignment -> !selectedSubjectIds.contains(assignment.getSubjectId()))
                .toList());

        Set<Long> currentSubjectIds = current.stream()
                .map(TeacherSubject::getSubjectId)
                .collect(java.util.stream.Collectors.toSet());
        selectedSubjectIds.stream()
                .filter(subjectId -> !currentSubjectIds.contains(subjectId))
                .forEach(subjectId -> {
                    TeacherSubject assignment = new TeacherSubject();
                    assignment.setTeacherId(teacherId);
                    assignment.setSubjectId(subjectId);
                    teacherSubjectRepository.save(assignment);
                });

        return getTeacherSubjects(teacherId);
    }

    @Transactional(readOnly = true)
    public List<Subject> getTeacherSubjects(Long teacherId, Long semesterId) {
        requireTeacher(teacherId);
        Set<Long> assignedSubjectIds = getTeacherSubjectIds(teacherId);
        if (assignedSubjectIds.isEmpty()) {
            return List.of();
        }

        Set<Long> offeredSubjectIds = semesterId == null
                ? null
                : semesterSubjectRepository.findBySemesterIdOrderByStartDateAscIdAsc(semesterId)
                        .stream()
                        .map(com.myfschool.entity.SemesterSubject::getSubjectId)
                        .collect(java.util.stream.Collectors.toSet());
        return subjectRepository.findAll().stream()
                .filter(subject -> assignedSubjectIds.contains(subject.getId()))
                .filter(subject -> offeredSubjectIds == null || offeredSubjectIds.contains(subject.getId()))
                .sorted(Comparator.comparing(Subject::getSubjectCode))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminStudentResponse> getTeacherStudents(
            Long teacherId,
            Long subjectId,
            Long semesterId,
            String search
    ) {
        requireTeacher(teacherId);
        Set<Long> subjectIds = getTeacherSubjectIds(teacherId);
        if (subjectId != null) {
            requireTeacherCanTeach(teacherId, subjectId);
            subjectIds = Set.of(subjectId);
        }
        if (subjectIds.isEmpty()) {
            return List.of();
        }
        if (semesterId != null) {
            requireSemester(semesterId);
        }

        List<StudentSubjectEnrollment> enrollments = semesterId == null
                ? enrollmentRepository.findBySubjectIdInOrderByUserIdAscSemesterIdAscSubjectIdAsc(subjectIds)
                : enrollmentRepository.findBySemesterIdAndSubjectIdInOrderByUserIdAscSemesterIdAscSubjectIdAsc(
                        semesterId,
                        subjectIds
                );
        Set<Long> studentIds = enrollments.stream()
                .map(StudentSubjectEnrollment::getUserId)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        Map<Long, User> studentsById = userRepository.findAllById(studentIds)
                .stream()
                .collect(java.util.stream.Collectors.toMap(User::getId, Function.identity()));

        String keyword = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        return studentIds.stream()
                .map(studentsById::get)
                .filter(student -> student != null)
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

    @Transactional(readOnly = true)
    public List<AdminGradeResponse> getTeacherGrades(
            Long teacherId,
            Long userId,
            Long semesterId,
            Long subjectId
    ) {
        requireTeacher(teacherId);
        Set<Long> allowedSubjectIds = getTeacherSubjectIds(teacherId);
        if (subjectId != null) {
            requireTeacherCanTeach(teacherId, subjectId);
            allowedSubjectIds = Set.of(subjectId);
        }
        if (allowedSubjectIds.isEmpty()) {
            return List.of();
        }

        Set<Long> subjectFilter = allowedSubjectIds;
        return gradeRepository.findAll().stream()
                .filter(grade -> subjectFilter.contains(grade.getSubjectId()))
                .filter(grade -> userId == null || userId.equals(grade.getUserId()))
                .filter(grade -> semesterId == null || semesterId.equals(grade.getSemesterId()))
                .map(this::mapGrade)
                .sorted(Comparator
                        .comparing(AdminGradeResponse::subjectCode)
                        .thenComparing(AdminGradeResponse::studentCode))
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
    public AdminGradeResponse createGradeForTeacher(Long teacherId, SaveGradeRequest request) {
        requireTeacherCanTeach(teacherId, request.subjectId());
        return createGrade(request);
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
    public AdminGradeResponse updateGradeForTeacher(Long teacherId, Long id, SaveGradeRequest request) {
        StudentGrade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student grade", id));
        requireTeacherCanTeach(teacherId, grade.getSubjectId());
        requireTeacherCanTeach(teacherId, request.subjectId());
        gradeRepository.findByUserIdAndSubjectIdAndSemesterId(
                        request.userId(), request.subjectId(), request.semesterId()
                )
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ResourceAlreadyExistsException(
                            "Sinh viÃªn Ä‘Ã£ cÃ³ Ä‘iá»ƒm cho mÃ´n há»c trong há»c ká»³ nÃ y"
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

    @Transactional
    public void deleteGradeForTeacher(Long teacherId, Long id) {
        StudentGrade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student grade", id));
        requireTeacherCanTeach(teacherId, grade.getSubjectId());
        gradeRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public byte[] buildTeacherGradeTemplate(
            Long teacherId,
            Long semesterId,
            Long subjectId,
            String className
    ) {
        requireTeacherCanTeach(teacherId, subjectId);
        Semester semester = requireSemester(semesterId);
        Subject subject = requireSubject(subjectId);
        List<AdminStudentResponse> students = filterStudentsByClass(
                getTeacherStudents(teacherId, subjectId, semesterId, null),
                className
        );
        List<GradeComponent> components = resolveTemplateComponents(semesterId, subjectId);
        Map<Long, StudentGrade> gradesByStudentId = gradesByStudentId(semesterId, subjectId);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(safeSheetName(subject.getSubjectCode()));
            CellStyle headerStyle = headerStyle(workbook);

            Row header = sheet.createRow(0);
            writeHeader(header, 0, "Student ID", headerStyle);
            writeHeader(header, 1, "Mã sinh viên", headerStyle);
            writeHeader(header, 2, "Họ tên", headerStyle);
            writeHeader(header, 3, "Lớp", headerStyle);
            for (int index = 0; index < components.size(); index++) {
                GradeComponent component = components.get(index);
                writeHeader(
                        header,
                        GRADE_START_COLUMN + index,
                        component.name() + " (" + formatWeight(component.weight()) + "%)",
                        headerStyle
                );
            }

            int rowIndex = 1;
            for (AdminStudentResponse student : students) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(student.id());
                row.createCell(1).setCellValue(student.userName());
                row.createCell(2).setCellValue(student.fullName());
                row.createCell(3).setCellValue(student.className() == null ? "" : student.className());
                StudentGrade grade = gradesByStudentId.get(student.id());
                Map<String, BigDecimal> scores = grade == null ? Map.of() : scoresByItemName(grade);
                for (int index = 0; index < components.size(); index++) {
                    GradeComponent component = components.get(index);
                    BigDecimal score = scores.get(normalizeKey(component.name()));
                    if (score != null) {
                        row.createCell(GRADE_START_COLUMN + index).setCellValue(score.doubleValue());
                    }
                }
            }

            sheet.createFreezePane(0, 1);
            sheet.setColumnHidden(0, true);
            for (int column = 0; column < GRADE_START_COLUMN + components.size(); column++) {
                sheet.autoSizeColumn(column);
            }
            sheet.setColumnWidth(2, Math.max(sheet.getColumnWidth(2), 7200));
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException error) {
            throw new BadRequestException("Không thể tạo file template điểm");
        }
    }

    @Transactional
    public GradeImportResultResponse importTeacherGrades(
            Long teacherId,
            Long semesterId,
            Long subjectId,
            String className,
            MultipartFile file
    ) {
        requireTeacherCanTeach(teacherId, subjectId);
        requireSemester(semesterId);
        requireSubject(subjectId);
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File import không được để trống");
        }

        List<AdminStudentResponse> students = filterStudentsByClass(
                getTeacherStudents(teacherId, subjectId, semesterId, null),
                className
        );
        Map<Long, AdminStudentResponse> studentsById = students.stream()
                .collect(java.util.stream.Collectors.toMap(AdminStudentResponse::id, Function.identity()));
        Map<String, AdminStudentResponse> studentsByCode = students.stream()
                .collect(java.util.stream.Collectors.toMap(
                        student -> normalizeKey(student.userName()),
                        Function.identity(),
                        (left, right) -> left
                ));

        List<String> errors = new ArrayList<>();
        int importedRows = 0;
        int createdRows = 0;
        int updatedRows = 0;
        int skippedRows = 0;

        try (InputStream input = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(input)) {
            Sheet sheet = workbook.getNumberOfSheets() == 0 ? null : workbook.getSheetAt(0);
            if (sheet == null) {
                throw new BadRequestException("File Excel không có sheet dữ liệu");
            }
            Row header = sheet.getRow(0);
            List<GradeComponent> components = parseGradeComponents(header);
            DataFormatter formatter = new DataFormatter();

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (isBlankRow(row, formatter)) {
                    continue;
                }
                int excelRow = rowIndex + 1;
                try {
                    AdminStudentResponse student = resolveStudent(row, formatter, studentsById, studentsByCode);
                    if (student == null) {
                        errors.add("Dòng " + excelRow + ": sinh viên không thuộc danh sách lớp/môn đã chọn");
                        continue;
                    }
                    List<String> identityErrors = validateStudentIdentity(row, formatter, student);
                    if (!identityErrors.isEmpty()) {
                        errors.add("Dòng " + excelRow + ": " + String.join(", ", identityErrors));
                        continue;
                    }

                    List<GradeItemInput> items = new ArrayList<>();
                    boolean hasAnyScore = false;
                    boolean hasMissingScore = false;
                    for (int index = 0; index < components.size(); index++) {
                        GradeComponent component = components.get(index);
                        BigDecimal score = readDecimal(row.getCell(GRADE_START_COLUMN + index), formatter);
                        if (score == null) {
                            hasMissingScore = true;
                        } else {
                            hasAnyScore = true;
                        }
                        items.add(new GradeItemInput(
                                component.name(),
                                component.weight(),
                                score == null ? BigDecimal.ZERO : score
                        ));
                    }

                    if (!hasAnyScore) {
                        skippedRows++;
                        continue;
                    }
                    if (hasMissingScore) {
                        errors.add("Dòng " + excelRow + ": cần nhập đủ điểm thành phần");
                        continue;
                    }

                    SaveGradeRequest request = new SaveGradeRequest(
                            student.id(),
                            subjectId,
                            semesterId,
                            items
                    );
                    StudentGrade existing = gradeRepository.findByUserIdAndSubjectIdAndSemesterId(
                            student.id(),
                            subjectId,
                            semesterId
                    ).orElse(null);
                    saveGrade(existing == null ? new StudentGrade() : existing, request);
                    importedRows++;
                    if (existing == null) {
                        createdRows++;
                    } else {
                        updatedRows++;
                    }
                } catch (RuntimeException error) {
                    errors.add("Dòng " + excelRow + ": " + error.getMessage());
                }
            }
        } catch (IOException error) {
            throw new BadRequestException("Không thể đọc file Excel");
        }

        return new GradeImportResultResponse(
                importedRows,
                createdRows,
                updatedRows,
                skippedRows,
                errors.size(),
                errors
        );
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

    private List<AdminStudentResponse> filterStudentsByClass(
            List<AdminStudentResponse> students,
            String className
    ) {
        String normalizedClass = className == null ? "" : className.trim();
        if (normalizedClass.isBlank() || "ALL".equalsIgnoreCase(normalizedClass)) {
            return students;
        }
        if ("UNASSIGNED".equalsIgnoreCase(normalizedClass)) {
            return students.stream()
                    .filter(student -> student.className() == null || student.className().isBlank())
                    .toList();
        }
        return students.stream()
                .filter(student -> student.className() != null
                        && student.className().trim().equalsIgnoreCase(normalizedClass))
                .toList();
    }

    private Map<Long, StudentGrade> gradesByStudentId(Long semesterId, Long subjectId) {
        return gradeRepository.findAll().stream()
                .filter(grade -> semesterId.equals(grade.getSemesterId()))
                .filter(grade -> subjectId.equals(grade.getSubjectId()))
                .collect(java.util.stream.Collectors.toMap(
                        StudentGrade::getUserId,
                        Function.identity(),
                        (left, right) -> left
                ));
    }

    private Map<String, BigDecimal> scoresByItemName(StudentGrade grade) {
        Map<String, BigDecimal> scores = new LinkedHashMap<>();
        itemRepository.findByStudentGradeIdOrderByDisplayOrderAscIdAsc(grade.getId())
                .forEach(item -> scores.put(normalizeKey(item.getGradeItem()), parseScore(item.getValue())));
        return scores;
    }

    private List<GradeComponent> resolveTemplateComponents(Long semesterId, Long subjectId) {
        return gradeRepository.findAll().stream()
                .filter(grade -> semesterId.equals(grade.getSemesterId()))
                .filter(grade -> subjectId.equals(grade.getSubjectId()))
                .map(grade -> itemRepository.findByStudentGradeIdOrderByDisplayOrderAscIdAsc(grade.getId()))
                .filter(items -> !items.isEmpty())
                .findFirst()
                .map(items -> items.stream()
                        .map(item -> new GradeComponent(
                                item.getGradeItem(),
                                item.getWeight().setScale(2, RoundingMode.HALF_UP)
                        ))
                        .toList())
                .orElse(DEFAULT_GRADE_COMPONENTS);
    }

    private void writeHeader(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private CellStyle headerStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private String safeSheetName(String value) {
        String text = value == null || value.isBlank() ? "Nhap diem" : value.trim();
        return text.replaceAll("[\\\\/?*\\[\\]:]", "-");
    }

    private String formatWeight(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }

    private List<GradeComponent> parseGradeComponents(Row header) {
        if (header == null) {
            throw new BadRequestException("File Excel thiếu dòng tiêu đề");
        }
        DataFormatter formatter = new DataFormatter();
        List<GradeComponent> components = new ArrayList<>();
        for (int column = GRADE_START_COLUMN; column < header.getLastCellNum(); column++) {
            String value = formatter.formatCellValue(header.getCell(column)).trim();
            if (value.isBlank()) {
                continue;
            }
            components.add(parseGradeComponent(value));
        }
        if (components.isEmpty()) {
            throw new BadRequestException("File Excel không có cột điểm thành phần");
        }
        BigDecimal weight = components.stream()
                .map(GradeComponent::weight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (weight.compareTo(TOTAL_WEIGHT) != 0) {
            throw new BadRequestException("Tổng trọng số trong file Excel phải bằng 100%");
        }
        return components;
    }

    private GradeComponent parseGradeComponent(String header) {
        int open = header.lastIndexOf('(');
        int percent = header.lastIndexOf('%');
        if (open <= 0 || percent <= open) {
            throw new BadRequestException("Cột điểm không đúng định dạng: " + header);
        }
        String name = header.substring(0, open).trim();
        String weightText = header.substring(open + 1, percent).trim().replace(",", ".");
        if (name.isBlank()) {
            throw new BadRequestException("Tên đầu điểm trong file Excel không hợp lệ");
        }
        try {
            return new GradeComponent(name, new BigDecimal(weightText).setScale(2, RoundingMode.HALF_UP));
        } catch (NumberFormatException error) {
            throw new BadRequestException("Trọng số điểm không hợp lệ: " + header);
        }
    }

    private boolean isBlankRow(Row row, DataFormatter formatter) {
        if (row == null) {
            return true;
        }
        for (int column = 0; column < Math.max(row.getLastCellNum(), GRADE_START_COLUMN + 1); column++) {
            if (!formatter.formatCellValue(row.getCell(column)).trim().isBlank()) {
                return false;
            }
        }
        return true;
    }

    private AdminStudentResponse resolveStudent(
            Row row,
            DataFormatter formatter,
            Map<Long, AdminStudentResponse> studentsById,
            Map<String, AdminStudentResponse> studentsByCode
    ) {
        Long studentId = readLong(row.getCell(0), formatter);
        if (studentId != null && studentsById.containsKey(studentId)) {
            return studentsById.get(studentId);
        }
        String studentCode = formatter.formatCellValue(row.getCell(1)).trim();
        return studentsByCode.get(normalizeKey(studentCode));
    }

    private List<String> validateStudentIdentity(
            Row row,
            DataFormatter formatter,
            AdminStudentResponse student
    ) {
        List<String> errors = new ArrayList<>();
        String studentCode = formatter.formatCellValue(row.getCell(1)).trim();
        String fullName = formatter.formatCellValue(row.getCell(2)).trim();
        String className = formatter.formatCellValue(row.getCell(3)).trim();

        if (!sameDisplayText(studentCode, student.userName())) {
            errors.add("mã sinh viên không khớp dữ liệu hệ thống");
        }
        if (!sameDisplayText(fullName, student.fullName())) {
            errors.add("họ tên không khớp dữ liệu hệ thống");
        }
        if (!sameDisplayText(className, student.className())) {
            errors.add("lớp không khớp dữ liệu hệ thống");
        }
        return errors;
    }

    private boolean sameDisplayText(String left, String right) {
        return normalizeDisplayText(left).equals(normalizeDisplayText(right));
    }

    private String normalizeDisplayText(String value) {
        return value == null
                ? ""
                : value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private Long readLong(Cell cell, DataFormatter formatter) {
        if (cell == null) {
            return null;
        }
        String text = formatter.formatCellValue(cell).trim();
        if (text.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(text.replace(",", ".")).longValue();
        } catch (NumberFormatException error) {
            return null;
        }
    }

    private BigDecimal readDecimal(Cell cell, DataFormatter formatter) {
        if (cell == null) {
            return null;
        }
        String text = formatter.formatCellValue(cell).trim();
        if (text.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(text.replace(",", ".")).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException error) {
            throw new BadRequestException("Giá trị điểm không hợp lệ: " + text);
        }
    }

    private String normalizeKey(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private void validateItems(List<GradeItemInput> items) {
        for (GradeItemInput item : items) {
            if (item.name() == null || item.name().isBlank()) {
                throw new BadRequestException("Tên đầu điểm không được để trống");
            }
            if (item.weight() == null
                    || item.weight().compareTo(new BigDecimal("0.01")) < 0
                    || item.weight().compareTo(TOTAL_WEIGHT) > 0) {
                throw new BadRequestException("Trọng số đầu điểm phải nằm trong khoảng 0.01 đến 100");
            }
            if (item.score() == null
                    || item.score().compareTo(BigDecimal.ZERO) < 0
                    || item.score().compareTo(new BigDecimal("10.00")) > 0) {
                throw new BadRequestException("Điểm thành phần phải nằm trong khoảng 0 đến 10");
            }
        }
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
        if (!hasRole(user, "STUDENT")) {
            throw new BadRequestException("Người dùng được chọn không phải sinh viên");
        }
        return user;
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

    private Set<Long> getTeacherSubjectIds(Long teacherId) {
        return teacherSubjectRepository.findByTeacherIdOrderByIdAsc(teacherId)
                .stream()
                .map(TeacherSubject::getSubjectId)
                .collect(java.util.stream.Collectors.toSet());
    }

    private void requireTeacherCanTeach(Long teacherId, Long subjectId) {
        requireTeacher(teacherId);
        requireSubject(subjectId);
        if (!teacherSubjectRepository.existsByTeacherIdAndSubjectId(teacherId, subjectId)) {
            throw new BadRequestException("Teacher is not assigned to this subject");
        }
    }

    private Subject requireSubject(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", id));
    }

    private Semester requireSemester(Long id) {
        return semesterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Semester", id));
    }

    private String fullName(User user) {
        return java.util.stream.Stream.of(user.getFirstName(), user.getLastName())
                .filter(value -> value != null && !value.isBlank())
                .collect(java.util.stream.Collectors.joining(" "));
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

    private AdminTeacherResponse mapTeacher(User user) {
        return new AdminTeacherResponse(
                user.getId(),
                user.getUserName(),
                fullName(user),
                user.getEmail(),
                user.getPhone(),
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

    private boolean matchesTeacher(AdminTeacherResponse teacher, String keyword) {
        if (keyword.isEmpty()) return true;
        return contains(teacher.userName(), keyword)
                || contains(teacher.fullName(), keyword)
                || contains(teacher.email(), keyword)
                || contains(teacher.phone(), keyword);
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

    private record GradeComponent(String name, BigDecimal weight) {
    }
}
