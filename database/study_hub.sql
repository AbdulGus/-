-- Итоговая схема StudyHub, объединённая из шести миграций Flyway.
-- Для обычного запуска приложения используйте пустую базу: Flyway применит миграции сам.

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(120) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(80) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER'
);

CREATE TABLE courses (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    published BOOLEAN NOT NULL DEFAULT FALSE,
    author_id BIGINT NOT NULL REFERENCES users(id)
);

CREATE TABLE lessons (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(160) NOT NULL,
    content TEXT NOT NULL,
    position INTEGER NOT NULL CHECK (position > 0),
    course_id BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    CONSTRAINT uk_lesson_course_position UNIQUE (course_id, position)
);

CREATE TABLE tags (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO tags(name) VALUES ('java'), ('spring'), ('sql');

CREATE TABLE course_tags (
    course_id BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    tag_id BIGINT NOT NULL REFERENCES tags(id) ON DELETE RESTRICT,
    PRIMARY KEY (course_id, tag_id)
);

CREATE INDEX idx_courses_author_id ON courses(author_id);
CREATE INDEX idx_courses_lower_title ON courses(LOWER(title));
CREATE INDEX idx_lessons_course_id ON lessons(course_id);

