UPDATE users
SET password_hash = '123456',
    phone = '0858111305',
    status = 'ACTIVE'
WHERE phone = '0858111305'
   OR student_code = '0858111305';
