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
    ('HE186409', 'he186409@myfschool.local', '123456', 'Nguyễn Minh', 'Anh', '0901000409', 'SE1911-JV', 'ACTIVE'),
    ('HE186410', 'he186410@myfschool.local', '123456', 'Trần Gia', 'Bảo', '0901000410', 'SE1911-JV', 'ACTIVE'),
    ('HE186411', 'he186411@myfschool.local', '123456', 'Lê Hoàng', 'Duy', '0901000411', 'SE1911-JV', 'ACTIVE'),
    ('HE186412', 'he186412@myfschool.local', '123456', 'Phạm Thu', 'Hà', '0901000412', 'SE1911-JV', 'ACTIVE'),
    ('HE186413', 'he186413@myfschool.local', '123456', 'Võ Quốc', 'Huy', '0901000413', 'SE1911-JV', 'ACTIVE'),
    ('HE186414', 'he186414@myfschool.local', '123456', 'Đặng Khánh', 'Linh', '0901000414', 'SE1911-JV', 'ACTIVE'),
    ('HE186415', 'he186415@myfschool.local', '123456', 'Bùi Đức', 'Nam', '0901000415', 'SE1911-JV', 'ACTIVE')
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
    'HE186409',
    'HE186410',
    'HE186411',
    'HE186412',
    'HE186413',
    'HE186414',
    'HE186415'
)
ON CONFLICT (user_id, role_id) DO NOTHING;
