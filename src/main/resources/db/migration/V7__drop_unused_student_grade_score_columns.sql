ALTER TABLE student_grades
    DROP COLUMN IF EXISTS process_score,
    DROP COLUMN IF EXISTS midterm_score,
    DROP COLUMN IF EXISTS final_score;
