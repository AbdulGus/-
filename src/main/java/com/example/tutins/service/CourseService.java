package com.example.tutins.service;

import com.example.tutins.dto.CourseDtos;
import com.example.tutins.dto.LessonDtos;
import com.example.tutins.entity.Course;
import com.example.tutins.entity.Tag;
import com.example.tutins.entity.User;
import com.example.tutins.exception.NotFoundException;
import com.example.tutins.repository.CourseRepository;
import com.example.tutins.repository.TagRepository;
import com.example.tutins.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseService {
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    @Transactional(readOnly = true)
    public List<CourseDtos.CourseSummary> findAll() {
        return courseRepository.findAll().stream().map(this::toSummary).toList();
    }

    @Transactional(readOnly = true)
    public CourseDtos.CourseResponse findById(Long id) {
        return toResponse(detailed(id));
    }

    @Transactional
    public CourseDtos.CourseResponse create(CourseDtos.CourseRequest request, String email) {
        User author = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Course course = new Course();
        course.setAuthor(author);
        apply(course, request);
        Course saved = courseRepository.save(course);
        log.info("Course created id={} author={}", saved.getId(), email);
        return toResponse(saved);
    }

    @Transactional
    public CourseDtos.CourseResponse update(Long id, CourseDtos.CourseRequest request, String email, boolean admin) {
        Course course = detailed(id);
        checkOwner(course, email, admin);
        apply(course, request);
        log.info("Course updated id={} by={}", id, email);
        return toResponse(course);
    }

    @Transactional
    public void delete(Long id, String email, boolean admin) {
        Course course = detailed(id);
        checkOwner(course, email, admin);
        courseRepository.delete(course);
        log.info("Course deleted id={} by={}", id, email);
    }

    private Course detailed(Long id) {
        return courseRepository.findDetailedById(id)
                .orElseThrow(() -> new NotFoundException("Курс не найден: " + id));
    }

    private void checkOwner(Course course, String email, boolean admin) {
        if (!admin && !course.getAuthor().getEmail().equalsIgnoreCase(email)) {
            throw new AccessDeniedException("USER может изменять только свои курсы; ADMIN может изменять любой курс");
        }
    }

    private void apply(Course course, CourseDtos.CourseRequest request) {
        course.setTitle(request.title().trim());
        course.setDescription(request.description().trim());
        course.setPublished(request.published());
        Set<Long> ids = request.tagIds() == null ? Set.of() : request.tagIds();
        Set<Tag> tags = new HashSet<>(tagRepository.findAllById(ids));
        if (tags.size() != ids.size()) throw new NotFoundException("Один или несколько тегов не найдены");
        course.setTags(tags);
    }

    private CourseDtos.CourseSummary toSummary(Course course) {
        return new CourseDtos.CourseSummary(course.getId(), course.getTitle(), course.getDescription(),
                course.isPublished());
    }

    private CourseDtos.CourseResponse toResponse(Course course) {
        return new CourseDtos.CourseResponse(course.getId(), course.getTitle(), course.getDescription(),
                course.isPublished(), course.getAuthor().getName(), names(course),
                course.getLessons().stream().map(lesson -> new LessonDtos.LessonResponse(
                        lesson.getId(), lesson.getTitle(), lesson.getContent(), lesson.getPosition())).toList());
    }

    private Set<String> names(Course course) {
        return course.getTags().stream().map(Tag::getName).collect(Collectors.toCollection(java.util.TreeSet::new));
    }
}
