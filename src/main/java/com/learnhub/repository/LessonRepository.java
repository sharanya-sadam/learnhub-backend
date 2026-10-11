package com.learnhub.repository;

import com.learnhub.entity.CourseModule;
import com.learnhub.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByModuleOrderByDisplayOrderAsc(CourseModule module);
    List<Lesson> findByModuleIdOrderByDisplayOrderAsc(Long moduleId);

    @Query("SELECT COUNT(l) FROM Lesson l WHERE l.module.course.id = :courseId AND l.published = true")
    long countPublishedLessonsByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT l FROM Lesson l WHERE l.module.course.id = :courseId AND l.published = true ORDER BY l.module.displayOrder ASC, l.displayOrder ASC")
    List<Lesson> findPublishedLessonsByCourseId(@Param("courseId") Long courseId);
}
