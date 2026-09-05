package com.example.tutins.service;

import com.example.tutins.dto.CourseDtos;
import com.example.tutins.entity.Course;
import com.example.tutins.entity.User;
import com.example.tutins.repository.CourseRepository;
import com.example.tutins.repository.TagRepository;
import com.example.tutins.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {
    @Mock CourseRepository courseRepository;
    @Mock UserRepository userRepository;
    @Mock TagRepository tagRepository;
    @InjectMocks CourseService courseService;

    @Test
    void createsCourseForCurrentUser() {
        User author = user("student@example.com");
        when(userRepository.findByEmailIgnoreCase(author.getEmail())).thenReturn(Optional.of(author));
        when(tagRepository.findAllById(Set.of())).thenReturn(java.util.List.of());
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> {
            Course course = invocation.getArgument(0);
            course.setId(10L);
            return course;
        });

        CourseDtos.CourseResponse result = courseService.create(
                new CourseDtos.CourseRequest(" Java ", " Основы языка ", false, Set.of()),
                author.getEmail());

        assertEquals(10L, result.id());
        assertEquals("Java", result.title());
        assertEquals("Student", result.authorName());
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    void userCannotUpdateAnotherUsersCourse() {
        Course course = new Course();
        course.setId(1L);
        course.setAuthor(user("owner@example.com"));
        course.setLessons(new ArrayList<>());
        course.setTags(new HashSet<>());
        when(courseRepository.findDetailedById(1L)).thenReturn(Optional.of(course));

        CourseDtos.CourseRequest request = new CourseDtos.CourseRequest(
                "Java", "Описание", false, Set.of());

        assertThrows(AccessDeniedException.class,
                () -> courseService.update(1L, request, "other@example.com", false));
    }

    private User user(String email) {
        User user = new User();
        user.setEmail(email);
        user.setName("Student");
        return user;
    }
}
