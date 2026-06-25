INSERT INTO semesters (id, name, school_year, start_date, end_date) VALUES
    (1, 'Spring 2026', '2025-2026', '2026-01-05', '2026-04-25'),
    (2, 'Summer 2026', '2025-2026', '2026-05-04', '2026-08-22'),
    (3, 'Fall 2026', '2026-2027', '2026-09-07', '2026-12-26')
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    school_year = EXCLUDED.school_year,
    start_date = EXCLUDED.start_date,
    end_date = EXCLUDED.end_date;

INSERT INTO subjects (id, subject_code, subject_name, credits) VALUES
    (1, 'PRM393', 'Mobile Programming', 3),
    (2, 'SWR302', 'Software Requirement', 3),
    (3, 'DBI202', 'Database Systems', 3),
    (4, 'PRO192', 'Object-Oriented Programming', 3)
ON CONFLICT (id) DO UPDATE SET
    subject_code = EXCLUDED.subject_code,
    subject_name = EXCLUDED.subject_name,
    credits = EXCLUDED.credits;

INSERT INTO student_grades (
    id,
    user_id,
    subject_id,
    semester_id,
    process_score,
    midterm_score,
    final_score,
    total_score,
    letter_grade
) VALUES
    (1, 1, 1, 2, 8.00, 7.50, 8.50, 8.10, 'B+'),
    (2, 1, 2, 2, 7.00, 8.00, 7.50, 7.50, 'B'),
    (3, 1, 3, 1, 8.50, 8.00, 9.00, 8.60, 'A'),
    (4, 1, 4, 1, 7.50, 7.00, 8.00, 7.60, 'B')
ON CONFLICT (id) DO UPDATE SET
    user_id = EXCLUDED.user_id,
    subject_id = EXCLUDED.subject_id,
    semester_id = EXCLUDED.semester_id,
    process_score = EXCLUDED.process_score,
    midterm_score = EXCLUDED.midterm_score,
    final_score = EXCLUDED.final_score,
    total_score = EXCLUDED.total_score,
    letter_grade = EXCLUDED.letter_grade;

INSERT INTO schedule (
    id,
    user_id,
    subject_id,
    semester_id,
    study_date,
    start_time,
    end_time,
    room,
    lecturer_name,
    note
) VALUES
    (1, 1, 1, 2, '2026-06-24', '07:30', '09:30', 'BE-301', 'Nguyen Van A', 'Lab mobile'),
    (2, 1, 2, 2, '2026-06-25', '09:45', '11:45', 'BE-204', 'Tran Thi B', 'Group discussion'),
    (3, 1, 3, 2, '2026-06-26', '12:30', '14:30', 'BE-112', 'Le Van C', 'SQL practice'),
    (4, 1, 4, 2, '2026-06-27', '15:00', '17:00', 'BE-410', 'Pham Thi D', 'OOP exercise')
ON CONFLICT (id) DO UPDATE SET
    user_id = EXCLUDED.user_id,
    subject_id = EXCLUDED.subject_id,
    semester_id = EXCLUDED.semester_id,
    study_date = EXCLUDED.study_date,
    start_time = EXCLUDED.start_time,
    end_time = EXCLUDED.end_time,
    room = EXCLUDED.room,
    lecturer_name = EXCLUDED.lecturer_name,
    note = EXCLUDED.note;

INSERT INTO events (
    id,
    title,
    description,
    location,
    start_time,
    end_time,
    image_url,
    status
) VALUES
    (1, 'Workshop Mobile App', 'Build a simple mobile application', 'Hall A', '2026-06-28 08:00:00', '2026-06-28 11:00:00', NULL, 'ACTIVE'),
    (2, 'Career Talk', 'Company sharing and career orientation', 'Hall B', '2026-07-02 13:30:00', '2026-07-02 16:00:00', NULL, 'ACTIVE'),
    (3, 'Sports Day', 'Student sport activities', 'Stadium', '2026-07-05 07:00:00', '2026-07-05 17:00:00', NULL, 'ACTIVE')
ON CONFLICT (id) DO UPDATE SET
    title = EXCLUDED.title,
    description = EXCLUDED.description,
    location = EXCLUDED.location,
    start_time = EXCLUDED.start_time,
    end_time = EXCLUDED.end_time,
    image_url = EXCLUDED.image_url,
    status = EXCLUDED.status;

INSERT INTO application_types (id, name, description) VALUES
    (1, 'Leave request', 'Request leave from class'),
    (2, 'Transcript request', 'Request student transcript'),
    (3, 'Certificate request', 'Request student certificate')
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description;

INSERT INTO student_applications (
    id,
    user_id,
    application_type_id,
    title,
    content,
    status,
    response_note,
    updated_at
) VALUES
    (1, 1, 1, 'Leave request for PRM393', 'I request leave for one class session.', 'PENDING', NULL, NULL),
    (2, 1, 2, 'Transcript request', 'I need a transcript for scholarship application.', 'APPROVED', 'Approved by academic office', CURRENT_TIMESTAMP),
    (3, 1, 3, 'Student certificate request', 'I need a student certificate for internship.', 'PENDING', NULL, NULL)
ON CONFLICT (id) DO UPDATE SET
    user_id = EXCLUDED.user_id,
    application_type_id = EXCLUDED.application_type_id,
    title = EXCLUDED.title,
    content = EXCLUDED.content,
    status = EXCLUDED.status,
    response_note = EXCLUDED.response_note,
    updated_at = EXCLUDED.updated_at;

INSERT INTO clubs (
    id,
    name,
    description,
    leader_name,
    contact_email,
    image_url,
    status
) VALUES
    (1, 'F-Code', 'Programming club', 'Nguyen Code', 'fcode@myfschool.local', NULL, 'ACTIVE'),
    (2, 'F-English', 'English communication club', 'Tran English', 'fenglish@myfschool.local', NULL, 'ACTIVE'),
    (3, 'F-Sport', 'Sports club', 'Le Sport', 'fsport@myfschool.local', NULL, 'ACTIVE')
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    leader_name = EXCLUDED.leader_name,
    contact_email = EXCLUDED.contact_email,
    image_url = EXCLUDED.image_url,
    status = EXCLUDED.status;

INSERT INTO club_members (id, club_id, user_id, role, status) VALUES
    (1, 1, 1, 'MEMBER', 'ACTIVE'),
    (2, 2, 1, 'MEMBER', 'ACTIVE'),
    (3, 3, 1, 'MEMBER', 'ACTIVE')
ON CONFLICT (id) DO UPDATE SET
    club_id = EXCLUDED.club_id,
    user_id = EXCLUDED.user_id,
    role = EXCLUDED.role,
    status = EXCLUDED.status;

INSERT INTO password_reset_tokens (
    id,
    user_id,
    token,
    expired_at,
    used
) VALUES (
    1,
    1,
    'demo-reset-token-0858111305',
    '2026-12-31 23:59:59',
    FALSE
)
ON CONFLICT (id) DO UPDATE SET
    user_id = EXCLUDED.user_id,
    token = EXCLUDED.token,
    expired_at = EXCLUDED.expired_at,
    used = EXCLUDED.used;

SELECT setval(pg_get_serial_sequence('password_reset_tokens', 'id'), COALESCE((SELECT MAX(id) FROM password_reset_tokens), 1));
SELECT setval(pg_get_serial_sequence('semesters', 'id'), COALESCE((SELECT MAX(id) FROM semesters), 1));
SELECT setval(pg_get_serial_sequence('subjects', 'id'), COALESCE((SELECT MAX(id) FROM subjects), 1));
SELECT setval(pg_get_serial_sequence('student_grades', 'id'), COALESCE((SELECT MAX(id) FROM student_grades), 1));
SELECT setval(pg_get_serial_sequence('schedule', 'id'), COALESCE((SELECT MAX(id) FROM schedule), 1));
SELECT setval(pg_get_serial_sequence('events', 'id'), COALESCE((SELECT MAX(id) FROM events), 1));
SELECT setval(pg_get_serial_sequence('application_types', 'id'), COALESCE((SELECT MAX(id) FROM application_types), 1));
SELECT setval(pg_get_serial_sequence('student_applications', 'id'), COALESCE((SELECT MAX(id) FROM student_applications), 1));
SELECT setval(pg_get_serial_sequence('clubs', 'id'), COALESCE((SELECT MAX(id) FROM clubs), 1));
SELECT setval(pg_get_serial_sequence('club_members', 'id'), COALESCE((SELECT MAX(id) FROM club_members), 1));
