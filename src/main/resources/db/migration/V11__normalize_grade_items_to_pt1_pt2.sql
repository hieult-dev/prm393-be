DELETE FROM student_grade_items;

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
    seed.grade_category,
    seed.grade_item,
    seed.weight,
    COALESCE(grade.total_score::TEXT, '0.0'),
    seed.display_order
FROM student_grades grade
CROSS JOIN (
    VALUES
        ('pt1', 'pt1', 50.00, 10),
        ('pt2', 'pt2', 50.00, 20)
) AS seed(grade_category, grade_item, weight, display_order);
