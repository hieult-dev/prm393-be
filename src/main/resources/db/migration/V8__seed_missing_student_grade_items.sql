INSERT INTO student_grade_items (
    student_grade_id,
    grade_category,
    grade_item,
    weight,
    value,
    display_order
)
SELECT grade.id, seed.grade_category, seed.grade_item, seed.weight, seed.value, seed.display_order
FROM student_grades grade
JOIN subjects subject ON subject.id = grade.subject_id
JOIN (
    VALUES
        ('SWR302', 'Participation', 'Participation', 10.00, '8.0', 10),
        ('SWR302', 'Quiz', 'Quiz 1', 10.00, '7.0', 20),
        ('SWR302', 'Quiz', 'Quiz 2', 10.00, '8.0', 30),
        ('SWR302', 'Assignment', 'Requirement document', 30.00, '7.5', 40),
        ('SWR302', 'Final exam', 'Final exam', 40.00, '7.5', 50),
        ('SWR302', 'Course total', 'Average', NULL, '7.5', 60),
        ('SWR302', 'Course total', 'Status', NULL, 'Passed', 70),
        ('DBI202', 'Lab', 'Lab 1', 10.00, '8.5', 10),
        ('DBI202', 'Lab', 'Lab 2', 10.00, '8.0', 20),
        ('DBI202', 'Assignment', 'Database assignment', 20.00, '9.0', 30),
        ('DBI202', 'Progress test', 'Progress test', 20.00, '8.5', 40),
        ('DBI202', 'Final exam', 'Final exam', 40.00, '8.8', 50),
        ('DBI202', 'Course total', 'Average', NULL, '8.6', 60),
        ('DBI202', 'Course total', 'Status', NULL, 'Passed', 70),
        ('PRO192', 'Lab', 'Lab exercises', 20.00, '7.5', 10),
        ('PRO192', 'Assignment', 'OOP assignment', 20.00, '8.0', 20),
        ('PRO192', 'Progress test', 'Progress test', 20.00, '7.0', 30),
        ('PRO192', 'Final exam', 'Final exam', 40.00, '7.8', 40),
        ('PRO192', 'Course total', 'Average', NULL, '7.6', 50),
        ('PRO192', 'Course total', 'Status', NULL, 'Passed', 60)
) AS seed(subject_code, grade_category, grade_item, weight, value, display_order)
    ON seed.subject_code = subject.subject_code
WHERE grade.user_id = 1
  AND NOT EXISTS (
      SELECT 1
      FROM student_grade_items existing
      WHERE existing.student_grade_id = grade.id
        AND existing.grade_category = seed.grade_category
        AND existing.grade_item = seed.grade_item
  );

INSERT INTO student_grade_items (
    student_grade_id,
    grade_category,
    grade_item,
    weight,
    value,
    display_order
)
SELECT grade.id, 'Course total', 'Average', NULL, COALESCE(grade.total_score::TEXT, '0.0'), 10
FROM student_grades grade
WHERE NOT EXISTS (
    SELECT 1
    FROM student_grade_items existing
    WHERE existing.student_grade_id = grade.id
);

INSERT INTO student_grade_items (
    student_grade_id,
    grade_category,
    grade_item,
    weight,
    value,
    display_order
)
SELECT
    grade.id,
    'Course total',
    'Status',
    NULL,
    CASE
        WHEN COALESCE(grade.total_score, 0) >= 5 THEN 'Passed'
        WHEN COALESCE(grade.total_score, 0) = 0 THEN 'Studying'
        ELSE 'Not passed'
    END,
    20
FROM student_grades grade
WHERE NOT EXISTS (
    SELECT 1
    FROM student_grade_items existing
    WHERE existing.student_grade_id = grade.id
      AND existing.grade_category = 'Course total'
      AND existing.grade_item = 'Status'
);
