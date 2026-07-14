INSERT INTO roles (role_name) VALUES
    ('PARENT'),
    ('TEACHER')
ON CONFLICT (role_name) DO NOTHING;

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
    VALUES
        (
            'parent01',
            'parent01@myfschool.local',
            '$2a$12$pnPwVNeJUPBsCOLZhwi/JO0YuNiI9jazKD37ef6jwM8qgd7FLHN2C',
            'Parent',
            'Demo',
            '0902000001',
            NULL,
            'ACTIVE'
        ),
        (
            'teacher01',
            'teacher01@myfschool.local',
            '$2a$12$pnPwVNeJUPBsCOLZhwi/JO0YuNiI9jazKD37ef6jwM8qgd7FLHN2C',
            'Teacher',
            'Demo',
            '0902000002',
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

WITH role_links (user_name, role_name) AS (
    VALUES
        ('parent01', 'PARENT'),
        ('teacher01', 'TEACHER')
)
INSERT INTO user_role (user_id, role_id)
SELECT users.id, roles.id
FROM role_links
JOIN users ON users.user_name = role_links.user_name
JOIN roles ON roles.role_name = role_links.role_name
ON CONFLICT (user_id, role_id) DO NOTHING;

WITH teacher AS (
    SELECT id
    FROM users
    WHERE user_name = 'teacher01'
),
target_subjects AS (
    SELECT id
    FROM subjects
    WHERE subject_code IN ('PRM393', 'SWR302')
)
INSERT INTO teacher_subjects (teacher_id, subject_id)
SELECT teacher.id, target_subjects.id
FROM teacher
CROSS JOIN target_subjects
ON CONFLICT (teacher_id, subject_id) DO NOTHING;

WITH parent_account AS (
    SELECT id
    FROM users
    WHERE user_name = 'parent01'
),
target_students AS (
    SELECT users.id
    FROM users
    JOIN user_role ON user_role.user_id = users.id
    JOIN roles ON roles.id = user_role.role_id
    WHERE roles.role_name = 'STUDENT'
      AND users.user_name IN ('HE186409', 'HE186410')
)
INSERT INTO parent_students (parent_id, student_id)
SELECT parent_account.id, target_students.id
FROM parent_account
CROSS JOIN target_students
ON CONFLICT (parent_id, student_id) DO NOTHING;

WITH teacher AS (
    SELECT id, CONCAT_WS(' ', first_name, last_name) AS full_name
    FROM users
    WHERE user_name = 'teacher01'
),
target_subjects AS (
    SELECT id
    FROM subjects
    WHERE subject_code IN ('PRM393', 'SWR302')
)
UPDATE schedule
SET teacher_id = teacher.id,
    lecturer_name = teacher.full_name
FROM teacher, target_subjects
WHERE schedule.subject_id = target_subjects.id
  AND schedule.teacher_id IS NULL;
