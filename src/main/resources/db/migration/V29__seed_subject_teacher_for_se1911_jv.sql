WITH seed_users (
    user_name,
    email,
    user_password,
    first_name,
    last_name,
    phone,
    class_name,
    status
) AS (
    VALUES (
        'subject01',
        'subject01@myfschool.local',
        '$2a$12$pnPwVNeJUPBsCOLZhwi/JO0YuNiI9jazKD37ef6jwM8qgd7FLHN2C',
        'Subject',
        'Teacher',
        '0902000004',
        NULL,
        'ACTIVE'
    )
)
INSERT INTO users (
    user_name,
    email,
    user_password,
    first_name,
    last_name,
    phone,
    class_name,
    status
)
SELECT
    seed_users.user_name,
    seed_users.email,
    seed_users.user_password,
    seed_users.first_name,
    seed_users.last_name,
    seed_users.phone,
    seed_users.class_name,
    seed_users.status
FROM seed_users
ON CONFLICT (user_name) DO UPDATE SET
    email = EXCLUDED.email,
    user_password = EXCLUDED.user_password,
    first_name = EXCLUDED.first_name,
    last_name = EXCLUDED.last_name,
    phone = EXCLUDED.phone,
    class_name = EXCLUDED.class_name,
    status = EXCLUDED.status;

DELETE FROM user_role
USING users, roles
WHERE user_role.user_id = users.id
  AND user_role.role_id = roles.id
  AND users.user_name = 'subject01'
  AND roles.role_name IN ('TEACHER', 'HOMEROOM_TEACHER');

WITH role_links (user_name, role_name) AS (
    VALUES
        ('subject01', 'SUBJECT_TEACHER')
)
INSERT INTO user_role (user_id, role_id)
SELECT users.id, roles.id
FROM role_links
JOIN users ON users.user_name = role_links.user_name
JOIN roles ON roles.role_name = role_links.role_name
ON CONFLICT (user_id, role_id) DO NOTHING;

DELETE FROM homeroom_teacher_classes homeroom
USING users
WHERE homeroom.teacher_id = users.id
  AND users.user_name = 'subject01';

WITH teacher AS (
    SELECT id
    FROM users
    WHERE user_name = 'subject01'
),
target_subjects AS (
    SELECT id
    FROM subjects
    WHERE subject_code = 'DBI202'
)
INSERT INTO teacher_subjects (teacher_id, subject_id)
SELECT teacher.id, target_subjects.id
FROM teacher
CROSS JOIN target_subjects
ON CONFLICT (teacher_id, subject_id) DO NOTHING;

WITH teacher AS (
    SELECT id, CONCAT_WS(' ', first_name, last_name) AS full_name
    FROM users
    WHERE user_name = 'subject01'
),
target_subject AS (
    SELECT id
    FROM subjects
    WHERE subject_code = 'DBI202'
),
class_students AS (
    SELECT id
    FROM users
    WHERE class_name = 'SE1911-JV'
)
UPDATE schedule
SET teacher_id = teacher.id,
    lecturer_name = teacher.full_name
FROM teacher, target_subject, class_students
WHERE schedule.user_id = class_students.id
  AND schedule.subject_id = target_subject.id;
