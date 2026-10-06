package com.example.tutins.service;

import com.example.tutins.dto.LessonDtos;
import com.example.tutins.entity.Course;
import com.example.tutins.entity.Lesson;
import com.example.tutins.exception.NotFoundException;
import com.example.tutins.repository.CourseRepository;
import com.example.tutins.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LessonService {
    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;

    @Transactional
    public LessonDtos.LessonResponse create(Long courseId, LessonDtos.LessonRequest request,
                                            String email, boolean admin) {
        Course course = course(courseId);
        checkOwner(course, email, admin);
        Lesson lesson = new Lesson();
        lesson.setCourse(course);
        apply(lesson, request);
        lessonRepository.save(lesson);
        return response(lesson);
    }

    @Transactional
    public LessonDtos.LessonResponse update(Long courseId, Long lessonId, LessonDtos.LessonRequest request,
                                            String email, boolean admin) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new NotFoundException("Урок не найден: " + lessonId));
        if (!lesson.getCourse().getId().equals(courseId)) throw new NotFoundException("Урок не относится к курсу");
        checkOwner(lesson.getCourse(), email, admin);
        apply(lesson, request);
        return response(lesson);
    }

    @Transactional
    public void delete(Long courseId, Long lessonId, String email, boolean admin) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new NotFoundException("Урок не найден: " + lessonId));
        if (!lesson.getCourse().getId().equals(courseId)) throw new NotFoundException("Урок не относится к курсу");
        checkOwner(lesson.getCourse(), email, admin);
        lessonRepository.delete(lesson);
    }

    private Course course(Long id) {
        return courseRepository.findById(id).orElseThrow(() -> new NotFoundException("Курс не найден: " + id));
    }

    private void checkOwner(Course course, String email, boolean admin) {
        if (!admin && !course.getAuthor().getEmail().equalsIgnoreCase(email)) {
            throw new AccessDeniedException("Можно изменять уроки только своего курса");
        }
    }

    private void apply(Lesson lesson, LessonDtos.LessonRequest request) {
        lesson.setTitle(request.title().trim());
        lesson.setContent(request.content().trim());
        lesson.setPosition(request.position());
    }

    private LessonDtos.LessonResponse response(Lesson lesson) {
        return new LessonDtos.LessonResponse(lesson.getId(), lesson.getTitle(), lesson.getContent(), lesson.getPosition());
    }
}
