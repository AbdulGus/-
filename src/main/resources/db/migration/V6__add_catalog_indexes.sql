-- Дополнительная миграция изменяет уже существующую схему.
CREATE INDEX idx_courses_author_id ON courses(author_id);
CREATE INDEX idx_courses_lower_title ON courses(LOWER(title));
CREATE INDEX idx_lessons_course_id ON lessons(course_id);
