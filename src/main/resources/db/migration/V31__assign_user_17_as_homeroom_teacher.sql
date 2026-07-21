INSERT INTO roles (role_name) VALUES
    ('HOMEROOM_TEACHER')
ON CONFLICT (role_name) DO NOTHING;

WITH target_teacher AS (
    SELECT id
    FROM users
    WHERE id = 17
),
target_role AS (
    SELECT id
    FROM roles
    WHERE role_name = 'HOMEROOM_TEACHER'
)
INSERT INTO user_role (user_id, role_id)
SELECT target_teacher.id, target_role.id
FROM target_teacher
CROSS JOIN target_role
ON CONFLICT (user_id, role_id) DO NOTHING;

DELETE FROM homeroom_teacher_classes homeroom
WHERE LOWER(homeroom.class_name) = LOWER('SE1911-JV')
  AND homeroom.teacher_id <> 17;

INSERT INTO homeroom_teacher_classes (teacher_id, class_name)
SELECT users.id, 'SE1911-JV'
FROM users
WHERE users.id = 17
ON CONFLICT (teacher_id) DO UPDATE SET
    class_name = EXCLUDED.class_name,
    assigned_at = CURRENT_TIMESTAMP;
