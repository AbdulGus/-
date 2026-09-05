package com.example.tutins.repository;

import com.example.tutins.entity.Course;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    @EntityGraph(attributePaths = {"author", "lessons", "tags"})
    @Query("select distinct c from Course c where c.id = :id")
    Optional<Course> findDetailedById(@Param("id") Long id);
}
