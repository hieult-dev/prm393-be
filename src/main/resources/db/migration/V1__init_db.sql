CREATE TABLE users (
    id BIGINT IDENTITY(1,1) NOT NULL,
    student_code VARCHAR(20) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NULL,
    class_name VARCHAR(50) NULL,
    role VARCHAR(20) NOT NULL CONSTRAINT DF_users_role DEFAULT 'STUDENT',
    status VARCHAR(20) NOT NULL CONSTRAINT DF_users_status DEFAULT 'ACTIVE',
    created_at DATETIME2 NOT NULL CONSTRAINT DF_users_created_at DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT PK_users PRIMARY KEY (id),
    CONSTRAINT UQ_users_student_code UNIQUE (student_code),
    CONSTRAINT UQ_users_email UNIQUE (email)
);

CREATE TABLE password_reset_tokens (
    id BIGINT IDENTITY(1,1) NOT NULL,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL,
    expired_at DATETIME2 NOT NULL,
    used BIT NOT NULL CONSTRAINT DF_password_reset_tokens_used DEFAULT 0,
    created_at DATETIME2 NOT NULL CONSTRAINT DF_password_reset_tokens_created_at DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT PK_password_reset_tokens PRIMARY KEY (id),
    CONSTRAINT UQ_password_reset_tokens_token UNIQUE (token),
    CONSTRAINT FK_password_reset_tokens_users FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE semesters (
    id BIGINT IDENTITY(1,1) NOT NULL,
    name VARCHAR(50) NOT NULL,
    school_year VARCHAR(20) NOT NULL,
    start_date DATE NULL,
    end_date DATE NULL,
    CONSTRAINT PK_semesters PRIMARY KEY (id)
);

CREATE TABLE subjects (
    id BIGINT IDENTITY(1,1) NOT NULL,
    subject_code VARCHAR(20) NOT NULL,
    subject_name VARCHAR(100) NOT NULL,
    credits INT NOT NULL CONSTRAINT DF_subjects_credits DEFAULT 3,
    CONSTRAINT PK_subjects PRIMARY KEY (id),
    CONSTRAINT UQ_subjects_subject_code UNIQUE (subject_code)
);

CREATE TABLE student_grades (
    id BIGINT IDENTITY(1,1) NOT NULL,
    user_id BIGINT NOT NULL,
    subject_id BIGINT NOT NULL,
    semester_id BIGINT NOT NULL,
    process_score DECIMAL(4,2) NULL,
    midterm_score DECIMAL(4,2) NULL,
    final_score DECIMAL(4,2) NULL,
    total_score DECIMAL(4,2) NULL,
    letter_grade VARCHAR(5) NULL,
    CONSTRAINT PK_student_grades PRIMARY KEY (id),
    CONSTRAINT FK_student_grades_users FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT FK_student_grades_subjects FOREIGN KEY (subject_id) REFERENCES subjects(id),
    CONSTRAINT FK_student_grades_semesters FOREIGN KEY (semester_id) REFERENCES semesters(id)
);

CREATE TABLE schedule (
    id BIGINT IDENTITY(1,1) NOT NULL,
    user_id BIGINT NOT NULL,
    subject_id BIGINT NOT NULL,
    semester_id BIGINT NOT NULL,
    study_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    room VARCHAR(50) NULL,
    lecturer_name VARCHAR(100) NULL,
    note VARCHAR(MAX) NULL,
    CONSTRAINT PK_schedule PRIMARY KEY (id),
    CONSTRAINT FK_schedule_users FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT FK_schedule_subjects FOREIGN KEY (subject_id) REFERENCES subjects(id),
    CONSTRAINT FK_schedule_semesters FOREIGN KEY (semester_id) REFERENCES semesters(id)
);

CREATE TABLE events (
    id BIGINT IDENTITY(1,1) NOT NULL,
    title VARCHAR(150) NOT NULL,
    description VARCHAR(MAX) NULL,
    location VARCHAR(150) NULL,
    start_time DATETIME2 NOT NULL,
    end_time DATETIME2 NULL,
    image_url VARCHAR(MAX) NULL,
    status VARCHAR(20) NOT NULL CONSTRAINT DF_events_status DEFAULT 'ACTIVE',
    created_at DATETIME2 NOT NULL CONSTRAINT DF_events_created_at DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT PK_events PRIMARY KEY (id)
);

CREATE TABLE application_types (
    id BIGINT IDENTITY(1,1) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(MAX) NULL,
    CONSTRAINT PK_application_types PRIMARY KEY (id)
);

CREATE TABLE student_applications (
    id BIGINT IDENTITY(1,1) NOT NULL,
    user_id BIGINT NOT NULL,
    application_type_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    content VARCHAR(MAX) NOT NULL,
    status VARCHAR(20) NOT NULL CONSTRAINT DF_student_applications_status DEFAULT 'PENDING',
    response_note VARCHAR(MAX) NULL,
    created_at DATETIME2 NOT NULL CONSTRAINT DF_student_applications_created_at DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME2 NULL,
    CONSTRAINT PK_student_applications PRIMARY KEY (id),
    CONSTRAINT FK_student_applications_users FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT FK_student_applications_application_types FOREIGN KEY (application_type_id) REFERENCES application_types(id)
);

CREATE TABLE clubs (
    id BIGINT IDENTITY(1,1) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(MAX) NULL,
    leader_name VARCHAR(100) NULL,
    contact_email VARCHAR(100) NULL,
    image_url VARCHAR(MAX) NULL,
    status VARCHAR(20) NOT NULL CONSTRAINT DF_clubs_status DEFAULT 'ACTIVE',
    created_at DATETIME2 NOT NULL CONSTRAINT DF_clubs_created_at DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT PK_clubs PRIMARY KEY (id)
);

CREATE TABLE club_members (
    id BIGINT IDENTITY(1,1) NOT NULL,
    club_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL CONSTRAINT DF_club_members_role DEFAULT 'MEMBER',
    joined_at DATETIME2 NOT NULL CONSTRAINT DF_club_members_joined_at DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL CONSTRAINT DF_club_members_status DEFAULT 'ACTIVE',
    CONSTRAINT PK_club_members PRIMARY KEY (id),
    CONSTRAINT FK_club_members_clubs FOREIGN KEY (club_id) REFERENCES clubs(id),
    CONSTRAINT FK_club_members_users FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT UQ_club_members_club_id_user_id UNIQUE (club_id, user_id)
);
