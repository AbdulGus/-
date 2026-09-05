package com.example.tutins.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class LessonDtos {
    private LessonDtos() {}

    public record LessonRequest(
            @NotBlank @Size(max = 160) String title,
            @NotBlank String content,
            @Min(1) int position) {}

    public record LessonResponse(Long id, String title, String content, int position) {}
}
