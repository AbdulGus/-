package com.example.tutins.controller;

import com.example.tutins.dto.TagDtos;
import com.example.tutins.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@Tag(name = "Теги", description = "Справочник тегов")
public class TagController {
    private final TagService tagService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public List<TagDtos.TagResponse> findAll() {
        return tagService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Создать тег (только ADMIN)")
    public TagDtos.TagResponse create(@Valid @RequestBody TagDtos.TagRequest request) {
        return tagService.create(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Удалить неиспользуемый тег (только ADMIN)")
    public void delete(@PathVariable Long id) {
        tagService.delete(id);
    }
}
