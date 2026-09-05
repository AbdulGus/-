CREATE TABLE course_tags (
    course_id BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    tag_id BIGINT NOT NULL REFERENCES tags(id) ON DELETE RESTRICT,
    PRIMARY KEY (course_id, tag_id)
);
