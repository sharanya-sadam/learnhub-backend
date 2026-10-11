package com.learnhub.service;

import com.learnhub.dto.submission.GradeSubmissionRequest;
import com.learnhub.dto.submission.GradebookEntryDto;
import com.learnhub.dto.submission.SubmissionDto;
import com.learnhub.entity.*;
import com.learnhub.exception.BadRequestException;
import com.learnhub.exception.ResourceNotFoundException;
import com.learnhub.exception.UnauthorizedException;
import com.learnhub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GradeService {

    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AssignmentRepository assignmentRepository;
    private final CourseRepository courseRepository;

    @Transactional
    public SubmissionDto gradeSubmission(Long submissionId, GradeSubmissionRequest request, String userEmail) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found with ID: " + submissionId));

        User grader = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        // Security check: Only the course instructor or an administrator can grade
        Course course = submission.getAssignment().getCourse();
        if (!grader.getRole().equals(Role.ROLE_ADMIN) && !course.getInstructor().getId().equals(grader.getId())) {
            throw new UnauthorizedException("You do not have permission to grade submissions for this course");
        }

        BigDecimal marks = request.getMarksAwarded();
        BigDecimal maxMarks = submission.getAssignment().getMaxMarks();

        if (marks.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Marks cannot be negative");
        }
        if (marks.compareTo(maxMarks) > 0) {
            throw new BadRequestException("Marks cannot exceed the assignment maximum of " + maxMarks);
        }

        submission.setMarksAwarded(marks);
        submission.setFeedback(request.getFeedback());
        submission.setStatus("GRADED");
        submission.setGradedAt(LocalDateTime.now());
        submission.setGradedBy(grader);

        submission = submissionRepository.save(submission);
        return SubmissionDto.fromEntity(submission);
    }

    public List<GradebookEntryDto> getStudentGradebook(String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentEmail));

        List<Enrollment> enrollments = enrollmentRepository.findByStudentOrderByEnrolledAtDesc(student);
        List<GradebookEntryDto> entries = new ArrayList<>();

        for (Enrollment enrollment : enrollments) {
            Course course = enrollment.getCourse();
            List<Assignment> assignments = assignmentRepository.findByCourseIdAndPublishedTrueOrderByDueDateAsc(course.getId());

            for (Assignment assignment : assignments) {
                Optional<Submission> subOpt = submissionRepository.findByAssignmentIdAndStudentId(assignment.getId(), student.getId());

                if (subOpt.isPresent()) {
                    Submission sub = subOpt.get();
                    String gradingStatus = "GRADED".equalsIgnoreCase(sub.getStatus()) ? "GRADED" : "PENDING_GRADING";
                    entries.add(GradebookEntryDto.create(
                            assignment.getId(),
                            course.getId(),
                            course.getTitle(),
                            assignment.getTitle(),
                            assignment.getMaxMarks(),
                            sub.getMarksAwarded(),
                            sub.getStatus(), // "SUBMITTED" or "LATE" or "GRADED"
                            gradingStatus,
                            sub.getFeedback(),
                            sub.getGradedAt(),
                            sub.getSubmittedAt(),
                            assignment.getDueDate()
                    ));
                } else {
                    entries.add(GradebookEntryDto.create(
                            assignment.getId(),
                            course.getId(),
                            course.getTitle(),
                            assignment.getTitle(),
                            assignment.getMaxMarks(),
                            null, // null marks, avoid false zero!
                            "NOT_SUBMITTED",
                            "NOT_SUBMITTED",
                            null,
                            null,
                            null,
                            assignment.getDueDate()
                    ));
                }
            }
        }

        return entries;
    }

    public List<SubmissionDto> getCourseSubmissions(Long courseId, String userEmail) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        if (!user.getRole().equals(Role.ROLE_ADMIN) && !course.getInstructor().getId().equals(user.getId())) {
            throw new UnauthorizedException("You do not have permission to view grades for this course");
        }

        List<Submission> submissions = submissionRepository.findByCourseId(courseId);
        return submissions.stream().map(SubmissionDto::fromEntity).collect(Collectors.toList());
    }
}
