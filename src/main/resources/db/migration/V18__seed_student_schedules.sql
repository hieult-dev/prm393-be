WITH target_students AS (
    SELECT users.id AS user_id
    FROM users
    JOIN user_role ON user_role.user_id = users.id
    JOIN roles ON roles.id = user_role.role_id
    WHERE roles.role_name = 'STUDENT'
      AND users.status = 'ACTIVE'
      AND users.class_name = 'SE1911-JV'
),
target_semester AS (
    SELECT id AS semester_id
    FROM semesters
    WHERE name = 'Summer 2026'
),
target_subjects AS (
    SELECT
        target_semester.semester_id,
        subjects.id AS subject_id
    FROM (
        VALUES
            ('PRM393'),
            ('SWR302'),
            ('DBI202'),
            ('PRO192'),
            ('MSS301'),
            ('MLN111'),
            ('EXE201')
    ) AS subject_seed(subject_code)
    JOIN subjects ON subjects.subject_code = subject_seed.subject_code
    CROSS JOIN target_semester
    JOIN semester_subjects
        ON semester_subjects.semester_id = target_semester.semester_id
       AND semester_subjects.subject_id = subjects.id
)
INSERT INTO student_subject_enrollments (
    user_id,
    semester_id,
    subject_id
)
SELECT
    target_students.user_id,
    target_subjects.semester_id,
    target_subjects.subject_id
FROM target_students
CROSS JOIN target_subjects
ON CONFLICT (user_id, semester_id, subject_id) DO NOTHING;

WITH target_students AS (
    SELECT users.id AS user_id
    FROM users
    JOIN user_role ON user_role.user_id = users.id
    JOIN roles ON roles.id = user_role.role_id
    WHERE roles.role_name = 'STUDENT'
      AND users.status = 'ACTIVE'
      AND users.class_name = 'SE1911-JV'
),
target_semester AS (
    SELECT id AS semester_id
    FROM semesters
    WHERE name = 'Summer 2026'
),
schedule_slots AS (
    SELECT *
    FROM (
        VALUES
            ('PRM393', DATE '2026-07-13', TIME '07:30', TIME '09:30', 'BE-301', 'Nguyen Van A', 'Mobile lab'),
            ('MLN111', DATE '2026-07-13', TIME '12:30', TIME '14:30', 'BE-205', 'Tran Thi Mai', 'Lecture'),
            ('SWR302', DATE '2026-07-14', TIME '09:45', TIME '11:45', 'BE-204', 'Tran Thi B', 'Requirement workshop'),
            ('DBI202', DATE '2026-07-15', TIME '07:30', TIME '09:30', 'BE-112', 'Le Van C', 'SQL practice'),
            ('EXE201', DATE '2026-07-15', TIME '12:30', TIME '14:30', 'BE-402', 'Hoang Minh Quan', 'Project mentoring'),
            ('PRO192', DATE '2026-07-16', TIME '09:45', TIME '11:45', 'BE-410', 'Pham Thi D', 'OOP exercise'),
            ('MSS301', DATE '2026-07-17', TIME '07:30', TIME '09:30', 'BE-308', 'Dang Quoc Huy', 'Microservices practice'),
            ('PRM393', DATE '2026-07-20', TIME '07:30', TIME '09:30', 'BE-301', 'Nguyen Van A', 'Mobile lab'),
            ('MLN111', DATE '2026-07-20', TIME '12:30', TIME '14:30', 'BE-205', 'Tran Thi Mai', 'Lecture'),
            ('SWR302', DATE '2026-07-21', TIME '09:45', TIME '11:45', 'BE-204', 'Tran Thi B', 'Requirement workshop'),
            ('DBI202', DATE '2026-07-22', TIME '07:30', TIME '09:30', 'BE-112', 'Le Van C', 'SQL practice'),
            ('EXE201', DATE '2026-07-22', TIME '12:30', TIME '14:30', 'BE-402', 'Hoang Minh Quan', 'Project mentoring'),
            ('PRO192', DATE '2026-07-23', TIME '09:45', TIME '11:45', 'BE-410', 'Pham Thi D', 'OOP exercise'),
            ('MSS301', DATE '2026-07-24', TIME '07:30', TIME '09:30', 'BE-308', 'Dang Quoc Huy', 'Microservices practice')
    ) AS slot_seed(subject_code, study_date, start_time, end_time, room, lecturer_name, note)
),
resolved_slots AS (
    SELECT
        target_semester.semester_id,
        subjects.id AS subject_id,
        schedule_slots.study_date,
        schedule_slots.start_time,
        schedule_slots.end_time,
        schedule_slots.room,
        schedule_slots.lecturer_name,
        schedule_slots.note
    FROM schedule_slots
    JOIN subjects ON subjects.subject_code = schedule_slots.subject_code
    CROSS JOIN target_semester
    JOIN semester_subjects
        ON semester_subjects.semester_id = target_semester.semester_id
       AND semester_subjects.subject_id = subjects.id
    WHERE schedule_slots.study_date BETWEEN semester_subjects.start_date AND semester_subjects.end_date
)
INSERT INTO schedule (
    user_id,
    subject_id,
    semester_id,
    study_date,
    start_time,
    end_time,
    room,
    lecturer_name,
    note
)
SELECT
    target_students.user_id,
    resolved_slots.subject_id,
    resolved_slots.semester_id,
    resolved_slots.study_date,
    resolved_slots.start_time,
    resolved_slots.end_time,
    resolved_slots.room,
    resolved_slots.lecturer_name,
    resolved_slots.note
FROM target_students
JOIN resolved_slots ON TRUE
JOIN student_subject_enrollments
    ON student_subject_enrollments.user_id = target_students.user_id
   AND student_subject_enrollments.semester_id = resolved_slots.semester_id
   AND student_subject_enrollments.subject_id = resolved_slots.subject_id
WHERE NOT EXISTS (
    SELECT 1
    FROM schedule existing
    WHERE existing.user_id = target_students.user_id
      AND existing.subject_id = resolved_slots.subject_id
      AND existing.semester_id = resolved_slots.semester_id
      AND existing.study_date = resolved_slots.study_date
      AND existing.start_time = resolved_slots.start_time
      AND existing.end_time = resolved_slots.end_time
);
