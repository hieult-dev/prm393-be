INSERT INTO users (
    user_name,
    email,
    user_password,
    first_name,
    last_name,
    phone,
    class_name,
    status
) VALUES
    ('HE186416', 'he186416@myfschool.local', '$2a$12$pnPwVNeJUPBsCOLZhwi/JO0YuNiI9jazKD37ef6jwM8qgd7FLHN2C', 'Nguyen Thanh', 'Phong', '0901000416', 'SE1911-JV', 'ACTIVE'),
    ('HE186417', 'he186417@myfschool.local', '$2a$12$pnPwVNeJUPBsCOLZhwi/JO0YuNiI9jazKD37ef6jwM8qgd7FLHN2C', 'Tran Minh', 'Khoa', '0901000417', 'SE1911-JV', 'ACTIVE'),
    ('HE186418', 'he186418@myfschool.local', '$2a$12$pnPwVNeJUPBsCOLZhwi/JO0YuNiI9jazKD37ef6jwM8qgd7FLHN2C', 'Le Gia', 'Han', '0901000418', 'SE1911-JV', 'ACTIVE'),
    ('HE186419', 'he186419@myfschool.local', '$2a$12$pnPwVNeJUPBsCOLZhwi/JO0YuNiI9jazKD37ef6jwM8qgd7FLHN2C', 'Pham Quang', 'Minh', '0901000419', 'SE1911-JV', 'ACTIVE'),
    ('HE186420', 'he186420@myfschool.local', '$2a$12$pnPwVNeJUPBsCOLZhwi/JO0YuNiI9jazKD37ef6jwM8qgd7FLHN2C', 'Bui Anh', 'Thu', '0901000420', 'SE1911-JV', 'ACTIVE')
ON CONFLICT (user_name) DO UPDATE SET
    email = EXCLUDED.email,
    first_name = EXCLUDED.first_name,
    last_name = EXCLUDED.last_name,
    phone = EXCLUDED.phone,
    class_name = EXCLUDED.class_name,
    status = EXCLUDED.status;

INSERT INTO user_role (user_id, role_id)
SELECT users.id, roles.id
FROM users
JOIN roles ON roles.role_name = 'STUDENT'
WHERE users.user_name IN (
    'HE186416',
    'HE186417',
    'HE186418',
    'HE186419',
    'HE186420'
)
ON CONFLICT (user_id, role_id) DO NOTHING;
