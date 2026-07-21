UPDATE student_grade_items
SET grade_category = CASE
        WHEN LOWER(REPLACE(TRIM(grade_category), ' ', '')) = 'pt1' THEN 'Học kỳ 1'
        WHEN LOWER(REPLACE(TRIM(grade_category), ' ', '')) = 'pt2' THEN 'Học kỳ 2'
        ELSE grade_category
    END,
    grade_item = CASE
        WHEN LOWER(REPLACE(TRIM(grade_item), ' ', '')) = 'pt1' THEN 'Học kỳ 1'
        WHEN LOWER(REPLACE(TRIM(grade_item), ' ', '')) = 'pt2' THEN 'Học kỳ 2'
        ELSE grade_item
    END
WHERE LOWER(REPLACE(TRIM(grade_category), ' ', '')) IN ('pt1', 'pt2')
   OR LOWER(REPLACE(TRIM(grade_item), ' ', '')) IN ('pt1', 'pt2');
