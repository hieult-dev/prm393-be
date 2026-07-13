ALTER TABLE users RENAME CONSTRAINT UQ_users_student_code TO UQ_users_user_name;
ALTER TABLE users RENAME COLUMN student_code TO user_name;
ALTER TABLE users RENAME COLUMN full_name TO first_name;
ALTER TABLE users RENAME COLUMN password_hash TO user_password;

ALTER TABLE users ADD COLUMN last_name VARCHAR(50) NULL;

UPDATE users
SET last_name = CASE
        WHEN first_name LIKE '% %' THEN substring(first_name FROM '\S+$')
        ELSE NULL
    END,
    first_name = CASE
        WHEN first_name LIKE '% %' THEN regexp_replace(first_name, '\s+\S+$', '')
        ELSE first_name
    END;

ALTER TABLE users
    ALTER COLUMN user_name TYPE VARCHAR(250),
    ALTER COLUMN user_name DROP NOT NULL,
    ALTER COLUMN email TYPE VARCHAR(50),
    ALTER COLUMN email DROP NOT NULL,
    ALTER COLUMN user_password TYPE VARCHAR(250),
    ALTER COLUMN user_password DROP NOT NULL,
    ALTER COLUMN first_name TYPE VARCHAR(50),
    ALTER COLUMN first_name DROP NOT NULL;

ALTER TABLE permissions RENAME TO permission;
ALTER TABLE permission RENAME CONSTRAINT PK_permissions TO PK_permission;
ALTER TABLE permission RENAME CONSTRAINT UQ_permissions_permission_name TO UQ_permission_permission_name;
ALTER TABLE role_permission RENAME CONSTRAINT FK_role_permission_permissions TO FK_role_permission_permission;
