package com.learnhub.repository;

import com.learnhub.entity.Assignment;
import com.learnhub.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByCourseOrderByDueDateAsc(Course course);
    List<Assignment> findByCourseIdOrderByDueDateAsc(Long courseId);
    List<Assignment> findByCourseIdAndPublishedTrueOrderByDueDateAsc(Long courseId);

    @Query("SELECT a FROM Assignment a WHERE a.course.id IN :courseIds AND a.published = true AND a.dueDate >= :now ORDER BY a.dueDate ASC")
    List<Assignment> findUpcomingAssignmentsForCourses(@Param("courseIds") List<Long> courseIds, @Param("now") LocalDateTime now);

    @Query("SELECT a FROM Assignment a WHERE a.course.instructor.id = :instructorId ORDER BY a.dueDate ASC")
    List<Assignment> findAssignmentsByInstructorId(@Param("instructorId") Long instructorId);

    long countByCourseId(Long courseId);
}
