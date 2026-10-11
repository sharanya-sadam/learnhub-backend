package com.learnhub.repository;

import com.learnhub.entity.Assignment;
import com.learnhub.entity.Submission;
import com.learnhub.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    Optional<Submission> findByAssignmentAndStudent(Assignment assignment, User student);
    Optional<Submission> findByAssignmentIdAndStudentId(Long assignmentId, Long studentId);

    List<Submission> findByAssignmentOrderBySubmittedAtDesc(Assignment assignment);
    List<Submission> findByAssignmentIdOrderBySubmittedAtDesc(Long assignmentId);

    List<Submission> findByStudentOrderBySubmittedAtDesc(User student);
    List<Submission> findByStudentIdOrderBySubmittedAtDesc(Long studentId);

    @Query("SELECT s FROM Submission s WHERE s.assignment.course.id = :courseId ORDER BY s.submittedAt DESC")
    List<Submission> findByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT s FROM Submission s WHERE s.assignment.course.instructor.id = :instructorId AND s.status != 'GRADED' ORDER BY s.submittedAt DESC")
    List<Submission> findPendingSubmissionsForInstructor(@Param("instructorId") Long instructorId);

    long countByStatus(String status);

    @Query("SELECT COUNT(s) FROM Submission s WHERE s.assignment.course.instructor.id = :instructorId AND s.status != 'GRADED'")
    long countPendingSubmissionsByInstructorId(@Param("instructorId") Long instructorId);
}
