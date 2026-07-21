INSERT INTO roles (role_name) VALUES
    ('SUBJECT_TEACHER')
ON CONFLICT (role_name) DO NOTHING;

WITH target_teacher AS (
    SELECT id
    FROM users
    WHERE id = 17
),
target_role AS (
    SELECT id
    FROM roles
    WHERE role_name = 'SUBJECT_TEACHER'
)
INSERT INTO user_role (user_id, role_id)
SELECT target_teacher.id, target_role.id
FROM target_teacher
CROSS JOIN target_role
ON CONFLICT (user_id, role_id) DO NOTHING;

WITH target_teacher AS (
    SELECT id
    FROM users
    WHERE id = 17
),
target_subject AS (
    SELECT id
    FROM subjects
    WHERE subject_code = 'PRO192'
)
INSERT INTO teacher_subjects (teacher_id, subject_id)
SELECT target_teacher.id, target_subject.id
FROM target_teacher
CROSS JOIN target_subject
ON CONFLICT (teacher_id, subject_id) DO NOTHING;

WITH target_teacher AS (
    SELECT id, CONCAT_WS(' ', first_name, last_name) AS full_name
    FROM users
    WHERE id = 17
),
target_subject AS (
    SELECT id
    FROM subjects
    WHERE subject_code = 'PRO192'
),
class_students AS (
    SELECT id
    FROM users
    WHERE LOWER(class_name) = LOWER('SE1911-JV')
)
UPDATE schedule
SET teacher_id = target_teacher.id,
    lecturer_name = target_teacher.full_name
FROM target_teacher, target_subject, class_students
WHERE schedule.user_id = class_students.id
  AND schedule.subject_id = target_subject.id
  AND schedule.teacher_id IS NULL;
