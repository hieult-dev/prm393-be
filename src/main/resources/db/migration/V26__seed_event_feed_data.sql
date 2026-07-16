INSERT INTO events (
    id,
    title,
    description,
    location,
    start_time,
    end_time,
    image_url,
    status
) VALUES
    (
        101,
        'PRM393 Mobile Showcase',
        'Students present mobile app projects and receive feedback from lecturers.',
        'Innovation Hall',
        '2026-07-20 09:00:00',
        '2026-07-20 11:30:00',
        NULL,
        'ACTIVE'
    ),
    (
        102,
        'Homeroom Parent Meeting',
        'Homeroom teachers meet parents to share study progress and class updates.',
        'Room B-204',
        '2026-07-22 18:00:00',
        '2026-07-22 20:00:00',
        NULL,
        'ACTIVE'
    ),
    (
        103,
        'Career Orientation Day',
        'Companies and alumni share internship preparation and career paths.',
        'Hall A',
        '2026-07-26 08:30:00',
        '2026-07-26 12:00:00',
        NULL,
        'ACTIVE'
    ),
    (
        104,
        'Student Club Fair',
        'Student clubs introduce activities, recruitment plans, and upcoming workshops.',
        'Main Lobby',
        '2026-08-01 14:00:00',
        '2026-08-01 17:00:00',
        NULL,
        'ACTIVE'
    ),
    (
        105,
        'FPT Schools Sports Day',
        'Class teams join football, badminton, and running activities.',
        'Campus Stadium',
        '2026-08-08 07:30:00',
        '2026-08-08 16:30:00',
        NULL,
        'ACTIVE'
    )
ON CONFLICT (id) DO UPDATE SET
    title = EXCLUDED.title,
    description = EXCLUDED.description,
    location = EXCLUDED.location,
    start_time = EXCLUDED.start_time,
    end_time = EXCLUDED.end_time,
    image_url = EXCLUDED.image_url,
    status = EXCLUDED.status;

SELECT setval(
    pg_get_serial_sequence('events', 'id'),
    GREATEST(COALESCE((SELECT MAX(id) FROM events), 1), 105)
);
