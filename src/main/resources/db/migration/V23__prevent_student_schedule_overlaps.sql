DELETE FROM schedule
WHERE end_time <= start_time;

WITH ranked_overlaps AS (
    SELECT loser.id
    FROM schedule loser
    JOIN schedule winner
      ON winner.user_id = loser.user_id
     AND winner.study_date = loser.study_date
     AND winner.id <> loser.id
     AND loser.start_time < winner.end_time
     AND loser.end_time > winner.start_time
    CROSS JOIN LATERAL (
        SELECT
            (CASE
                WHEN NULLIF(TRIM(loser.room), '') IS NOT NULL
                 AND UPPER(TRIM(loser.room)) <> 'TBA' THEN 4
                ELSE 0
             END)
             + (CASE WHEN NULLIF(TRIM(loser.lecturer_name), '') IS NOT NULL THEN 2 ELSE 0 END)
             + (CASE WHEN NULLIF(TRIM(loser.note), '') IS NOT NULL THEN 1 ELSE 0 END) AS score,
            EXTRACT(EPOCH FROM loser.end_time - loser.start_time) AS duration_seconds
    ) loser_rank
    CROSS JOIN LATERAL (
        SELECT
            (CASE
                WHEN NULLIF(TRIM(winner.room), '') IS NOT NULL
                 AND UPPER(TRIM(winner.room)) <> 'TBA' THEN 4
                ELSE 0
             END)
             + (CASE WHEN NULLIF(TRIM(winner.lecturer_name), '') IS NOT NULL THEN 2 ELSE 0 END)
             + (CASE WHEN NULLIF(TRIM(winner.note), '') IS NOT NULL THEN 1 ELSE 0 END) AS score,
            EXTRACT(EPOCH FROM winner.end_time - winner.start_time) AS duration_seconds
    ) winner_rank
    WHERE winner_rank.score > loser_rank.score
       OR (
           winner_rank.score = loser_rank.score
           AND winner_rank.duration_seconds > loser_rank.duration_seconds
       )
       OR (
           winner_rank.score = loser_rank.score
           AND winner_rank.duration_seconds = loser_rank.duration_seconds
           AND winner.id > loser.id
       )
)
DELETE FROM schedule
WHERE id IN (SELECT id FROM ranked_overlaps);

CREATE INDEX IF NOT EXISTS IX_schedule_user_date_time
    ON schedule(user_id, study_date, start_time, end_time);

CREATE OR REPLACE FUNCTION validate_schedule_time_window()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.end_time <= NEW.start_time THEN
        RAISE EXCEPTION 'schedule end time must be after start time';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM schedule existing
        WHERE existing.user_id = NEW.user_id
          AND existing.study_date = NEW.study_date
          AND (NEW.id IS NULL OR existing.id <> NEW.id)
          AND NEW.start_time < existing.end_time
          AND NEW.end_time > existing.start_time
    ) THEN
        RAISE EXCEPTION 'student schedule cannot overlap existing schedule';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS TR_schedule_validate_time_window ON schedule;

CREATE TRIGGER TR_schedule_validate_time_window
BEFORE INSERT OR UPDATE OF user_id, study_date, start_time, end_time
ON schedule
FOR EACH ROW
EXECUTE FUNCTION validate_schedule_time_window();
