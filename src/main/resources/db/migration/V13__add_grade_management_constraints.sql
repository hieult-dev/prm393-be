CREATE UNIQUE INDEX IF NOT EXISTS UQ_student_grades_user_subject_semester
    ON student_grades(user_id, subject_id, semester_id);

INSERT INTO permission (permission_name, description) VALUES
    ('GRADE_READ', 'View student grades'),
    ('GRADE_WRITE', 'Create and update student grades')
ON CONFLICT (permission_name) DO NOTHING;

INSERT INTO role_permission (role_id, permission_id)
SELECT roles.id, permission.id
FROM roles
CROSS JOIN permission
WHERE roles.role_name = 'ADMIN'
  AND permission.permission_name IN ('GRADE_READ', 'GRADE_WRITE')
ON CONFLICT (role_id, permission_id) DO NOTHING;
