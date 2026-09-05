CREATE TABLE lessons (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(160) NOT NULL,
    content TEXT NOT NULL,
    position INTEGER NOT NULL CHECK (position > 0),
    course_id BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    CONSTRAINT uk_lesson_course_position UNIQUE (course_id, position)
);
