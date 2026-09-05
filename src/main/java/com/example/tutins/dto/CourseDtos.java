package com.example.tutins.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Set;

public final class CourseDtos {
    private CourseDtos() {}

    public record CourseRequest(
            @NotBlank @Size(max = 150) String title,
            @NotBlank @Size(max = 1000) String description,
            boolean published,
            Set<Long> tagIds) {}

    public record CourseSummary(
            Long id, String title, String description, boolean published) {}

    public record CourseResponse(
            Long id, String title, String description, boolean published,
            String authorName, Set<String> tags, List<LessonDtos.LessonResponse> lessons) {}
}
