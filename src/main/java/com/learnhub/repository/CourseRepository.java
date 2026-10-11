package com.learnhub.repository;

import com.learnhub.entity.Course;
import com.learnhub.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByInstructor(User instructor);

    Page<Course> findByInstructor(User instructor, Pageable pageable);

    long countByPublished(boolean published);

    @Query("SELECT c FROM Course c WHERE " +
           "(:publishedOnly = false OR c.published = true) AND " +
           "(:category IS NULL OR :category = '' OR c.category = :category) AND " +
           "(:difficulty IS NULL OR :difficulty = '' OR c.difficulty = :difficulty) AND " +
           "(:query IS NULL OR :query = '' OR LOWER(c.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(c.shortDescription) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Course> searchCourses(
            @Param("query") String query,
            @Param("category") String category,
            @Param("difficulty") String difficulty,
            @Param("publishedOnly") boolean publishedOnly,
            Pageable pageable
    );

    @Query("SELECT DISTINCT c.category FROM Course c WHERE c.published = true")
    List<String> findDistinctPublishedCategories();
}
