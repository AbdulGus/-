package com.example.tutins.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class TagDtos {
    private TagDtos() {}

    public record TagRequest(
            @NotBlank @Size(max = 50)
            @Pattern(regexp = "^[a-zA-Zа-яА-Я0-9+#.-]+$", message = "тег содержит недопустимые символы")
            String name) {}

    public record TagResponse(Long id, String name) {}
}
