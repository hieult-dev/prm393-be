WITH role_links (user_name, role_name) AS (
    VALUES
        ('teacher01', 'SUBJECT_TEACHER'),
        ('teacher01', 'HOMEROOM_TEACHER')
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

WITH teacher AS (
    SELECT id, CONCAT_WS(' ', first_name, last_name) AS full_name
    FROM users
    WHERE user_name = 'teacher01'
),
teacher_subject_assignments AS (
    SELECT teacher_subjects.subject_id
    FROM teacher_subjects
    JOIN teacher ON teacher.id = teacher_subjects.teacher_id
),
class_students AS (
    SELECT id
    FROM users
    WHERE class_name = 'SE1911-JV'
)
UPDATE schedule
SET teacher_id = teacher.id,
    lecturer_name = teacher.full_name
FROM teacher, teacher_subject_assignments, class_students
WHERE schedule.user_id = class_students.id
  AND schedule.subject_id = teacher_subject_assignments.subject_id;

DELETE FROM homeroom_teacher_classes homeroom
USING users
WHERE homeroom.teacher_id = users.id
  AND homeroom.class_name = 'SE1911-JV'
  AND users.user_name <> 'teacher01';

INSERT INTO homeroom_teacher_classes (teacher_id, class_name)
SELECT users.id, 'SE1911-JV'
FROM users
WHERE users.user_name = 'teacher01'
ON CONFLICT (teacher_id) DO UPDATE SET
    class_name = EXCLUDED.class_name;
