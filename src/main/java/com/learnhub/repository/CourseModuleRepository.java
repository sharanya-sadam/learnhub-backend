package com.learnhub.repository;

import com.learnhub.entity.Course;
import com.learnhub.entity.CourseModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseModuleRepository extends JpaRepository<CourseModule, Long> {
    List<CourseModule> findByCourseOrderByDisplayOrderAsc(Course course);
    List<CourseModule> findByCourseIdOrderByDisplayOrderAsc(Long courseId);
}
