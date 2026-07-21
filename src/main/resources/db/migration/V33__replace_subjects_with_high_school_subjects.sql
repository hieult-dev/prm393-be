WITH subject_updates (old_subject_code, new_subject_code, new_subject_name, credits) AS (
    VALUES
        ('PRM393', 'TOAN', 'Toán', 3),
        ('SWR302', 'NGUVAN', 'Ngữ văn', 3),
        ('DBI202', 'TIENGANH', 'Tiếng Anh', 3),
        ('PRO192', 'VATLY', 'Vật lý', 3),
        ('MSS301', 'HOAHOC', 'Hóa học', 3),
        ('MLN111', 'SINHHOC', 'Sinh học', 3),
        ('EXE201', 'LICHSU', 'Lịch sử', 3)
)
UPDATE subjects
SET subject_code = subject_updates.new_subject_code,
    subject_name = subject_updates.new_subject_name,
    credits = subject_updates.credits
FROM subject_updates
WHERE subjects.subject_code = subject_updates.old_subject_code;

INSERT INTO subjects (subject_code, subject_name, credits) VALUES
    ('DIALY', 'Địa lý', 3),
    ('TINHOC', 'Tin học', 3),
    ('CONGNGHE', 'Công nghệ', 3),
    ('GDKTPL', 'Giáo dục kinh tế và pháp luật', 3),
    ('GDTC', 'Giáo dục thể chất', 3),
    ('GDQPAN', 'Giáo dục quốc phòng và an ninh', 3)
ON CONFLICT (subject_code) DO UPDATE SET
    subject_name = EXCLUDED.subject_name,
    credits = EXCLUDED.credits;

SELECT setval(pg_get_serial_sequence('subjects', 'id'), COALESCE((SELECT MAX(id) FROM subjects), 1));
