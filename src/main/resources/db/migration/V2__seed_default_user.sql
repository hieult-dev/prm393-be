INSERT INTO users (
    student_code,
    full_name,
    email,
    password_hash,
    phone,
    class_name,
    role,
    status
) VALUES (
    '0858111305',
    'Default User',
    '0858111305@myfschool.local',
    '123456',
    '0858111305',
    NULL,
    'STUDENT',
    'ACTIVE'
)
ON CONFLICT (student_code) DO UPDATE SET
    full_name = EXCLUDED.full_name,
    email = EXCLUDED.email,
    password_hash = EXCLUDED.password_hash,
    phone = EXCLUDED.phone,
    class_name = EXCLUDED.class_name,
    role = EXCLUDED.role,
    status = EXCLUDED.status;
