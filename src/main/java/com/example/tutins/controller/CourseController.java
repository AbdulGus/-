package com.example.tutins.controller;

import com.example.tutins.dto.CourseDtos;
import com.example.tutins.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Tag(name = "Курсы", description = "Каталог и CRUD курсов")
public class CourseController {
    private final CourseService courseService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Получить список курсов")
    public List<CourseDtos.CourseSummary> findAll() {
        return courseService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Подробный курс; связи загружаются без N+1")
    public CourseDtos.CourseResponse findById(@PathVariable Long id) {
        return courseService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Создать курс")
    public CourseDtos.CourseResponse create(@Valid @RequestBody CourseDtos.CourseRequest request,
                                             Authentication authentication) {
        return courseService.create(request, authentication.getName());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Изменить свой курс; ADMIN может изменить любой")
    public CourseDtos.CourseResponse update(@PathVariable Long id,
                                             @Valid @RequestBody CourseDtos.CourseRequest request,
                                             Authentication authentication) {
        return courseService.update(id, request, authentication.getName(), isAdmin(authentication));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Удалить пустой курс")
    public void delete(@PathVariable Long id, Authentication authentication) {
        courseService.delete(id, authentication.getName(), isAdmin(authentication));
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
