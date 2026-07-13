UPDATE users
SET user_password = '$2a$12$pnPwVNeJUPBsCOLZhwi/JO0YuNiI9jazKD37ef6jwM8qgd7FLHN2C'
WHERE user_name IN (
    'HE186409',
    'HE186410',
    'HE186411',
    'HE186412',
    'HE186413',
    'HE186414',
    'HE186415'
)
  AND user_password = '123456';
