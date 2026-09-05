package com.example.tutins.controller;

import com.example.tutins.dto.LessonDtos;
import com.example.tutins.service.LessonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses/{courseId}/lessons")
@RequiredArgsConstructor
@Tag(name = "Уроки", description = "CRUD уроков внутри курса")
public class LessonController {
    private final LessonService lessonService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Добавить урок в свой курс")
    public LessonDtos.LessonResponse create(@PathVariable Long courseId,
                                             @Valid @RequestBody LessonDtos.LessonRequest request,
                                             Authentication authentication) {
        return lessonService.create(courseId, request, authentication.getName(), isAdmin(authentication));
    }

    @PutMapping("/{lessonId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Изменить урок")
    public LessonDtos.LessonResponse update(@PathVariable Long courseId, @PathVariable Long lessonId,
                                             @Valid @RequestBody LessonDtos.LessonRequest request,
                                             Authentication authentication) {
        return lessonService.update(courseId, lessonId, request, authentication.getName(), isAdmin(authentication));
    }

    @DeleteMapping("/{lessonId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Удалить урок")
    public void delete(@PathVariable Long courseId, @PathVariable Long lessonId,
                       Authentication authentication) {
        lessonService.delete(courseId, lessonId, authentication.getName(), isAdmin(authentication));
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
