UPDATE users
SET class_name = COALESCE(class_name, 'SE1911-JV')
WHERE id = 1;

INSERT INTO subjects (subject_code, subject_name, credits) VALUES
    ('MSS301', 'Microservices with Spring Cloud', 3),
    ('MLN111', 'Philosophy of Marxism - Leninism', 3),
    ('EXE201', 'Experiential Entrepreneurship 2', 3)
ON CONFLICT (subject_code) DO UPDATE SET
    subject_name = EXCLUDED.subject_name,
    credits = EXCLUDED.credits;

UPDATE student_grades
SET process_score = 0.00,
    midterm_score = 0.00,
    final_score = 0.00,
    total_score = 0.00,
    letter_grade = 'F'
WHERE user_id = 1
  AND semester_id = 2
  AND subject_id = (SELECT id FROM subjects WHERE subject_code = 'PRM393');

INSERT INTO student_grades (
    user_id,
    subject_id,
    semester_id,
    process_score,
    midterm_score,
    final_score,
    total_score,
    letter_grade
)
SELECT 1, subject_id, 2, process_score, midterm_score, final_score, total_score, letter_grade
FROM (
    VALUES
        ((SELECT id FROM subjects WHERE subject_code = 'MSS301'), 0.00, 0.00, 0.00, 0.00, 'F'),
        ((SELECT id FROM subjects WHERE subject_code = 'MLN111'), 7.30, 7.30, 7.30, 7.30, 'B'),
        ((SELECT id FROM subjects WHERE subject_code = 'EXE201'), 0.00, 0.00, 0.00, 0.00, 'F')
) AS seed(subject_id, process_score, midterm_score, final_score, total_score, letter_grade)
WHERE NOT EXISTS (
    SELECT 1
    FROM student_grades existing
    WHERE existing.user_id = 1
      AND existing.semester_id = 2
      AND existing.subject_id = seed.subject_id
);
