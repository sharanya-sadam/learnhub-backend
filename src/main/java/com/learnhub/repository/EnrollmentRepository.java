package com.learnhub.repository;

import com.learnhub.entity.Course;
import com.learnhub.entity.Enrollment;
import com.learnhub.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    boolean existsByStudentAndCourse(User student, Course course);
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    Optional<Enrollment> findByStudentAndCourse(User student, Course course);
    Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);

    List<Enrollment> findByStudentOrderByEnrolledAtDesc(User student);
    Page<Enrollment> findByStudentOrderByEnrolledAtDesc(User student, Pageable pageable);

    List<Enrollment> findByCourseOrderByEnrolledAtDesc(Course course);
    Page<Enrollment> findByCourseOrderByEnrolledAtDesc(Course course, Pageable pageable);

    long countByCourse(Course course);
    long countByCourseId(Long courseId);
    long countByStudent(User student);
}
