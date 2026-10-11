package com.learnhub.service;

import com.learnhub.dto.submission.CreateSubmissionRequest;
import com.learnhub.dto.submission.SubmissionDto;
import com.learnhub.entity.*;
import com.learnhub.exception.BadRequestException;
import com.learnhub.exception.ResourceNotFoundException;
import com.learnhub.exception.UnauthorizedException;
import com.learnhub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Transactional
    public SubmissionDto submitAssignment(Long assignmentId, CreateSubmissionRequest request, String studentEmail) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with ID: " + assignmentId));

        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentEmail));

        // Check enrollment
        boolean isEnrolled = enrollmentRepository.existsByStudentAndCourse(student, assignment.getCourse());
        if (!isEnrolled && !student.getRole().equals(Role.ROLE_ADMIN)) {
            throw new UnauthorizedException("You must be enrolled in the course to submit assignments");
        }

        LocalDateTime now = LocalDateTime.now();
        boolean isLate = now.isAfter(assignment.getDueDate());

        if (isLate && !assignment.isAllowLateSubmissions()) {
            throw new BadRequestException("Submission deadline has passed. Late submissions are not permitted for this assignment.");
        }

        // Validate submission contents according to allowed type
        String allowedType = assignment.getAllowedSubmissionType();
        boolean hasText = request.getTextResponse() != null && !request.getTextResponse().trim().isEmpty();
        boolean hasFile = request.getFilePath() != null && !request.getFilePath().trim().isEmpty();

        if ("TEXT".equalsIgnoreCase(allowedType) && !hasText) {
            throw new BadRequestException("Text response is required for this assignment");
        }
        if ("FILE".equalsIgnoreCase(allowedType) && !hasFile) {
            throw new BadRequestException("File upload is required for this assignment");
        }
        if ("BOTH".equalsIgnoreCase(allowedType) && !hasText && !hasFile) {
            throw new BadRequestException("Please provide a text response or upload a file for your submission");
        }

        String submissionStatus = isLate ? "LATE" : "SUBMITTED";

        Optional<Submission> existingOpt = submissionRepository.findByAssignmentIdAndStudentId(assignmentId, student.getId());
        Submission submission;

        if (existingOpt.isPresent()) {
            submission = existingOpt.get();
            submission.setTextResponse(request.getTextResponse());
            submission.setFilePath(request.getFilePath());
            submission.setOriginalFileName(request.getOriginalFileName());
            submission.setSubmissionVersion(submission.getSubmissionVersion() + 1);
            submission.setStatus(submissionStatus);
            submission.setSubmittedAt(now);
            // If re-submitted, reset grading so instructor re-reviews
            submission.setMarksAwarded(null);
            submission.setFeedback(null);
            submission.setGradedAt(null);
            submission.setGradedBy(null);
        } else {
            submission = Submission.builder()
                    .assignment(assignment)
                    .student(student)
                    .textResponse(request.getTextResponse())
                    .filePath(request.getFilePath())
                    .originalFileName(request.getOriginalFileName())
                    .submittedAt(now)
                    .status(submissionStatus)
                    .submissionVersion(1)
                    .build();
        }

        submission = submissionRepository.save(submission);
        return SubmissionDto.fromEntity(submission);
    }

    public SubmissionDto getMySubmission(Long assignmentId, String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentEmail));

        Submission submission = submissionRepository.findByAssignmentIdAndStudentId(assignmentId, student.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No submission found for this assignment"));

        return SubmissionDto.fromEntity(submission);
    }

    public List<SubmissionDto> getSubmissionsForAssignment(Long assignmentId, String userEmail) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with ID: " + assignmentId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        checkCourseOwnerOrAdmin(assignment.getCourse(), user);

        List<Submission> submissions = submissionRepository.findByAssignmentIdOrderBySubmittedAtDesc(assignmentId);
        return submissions.stream().map(SubmissionDto::fromEntity).collect(Collectors.toList());
    }

    public SubmissionDto getSubmissionById(Long submissionId, String userEmail) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found with ID: " + submissionId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        // Security check: only the student who submitted OR the course instructor OR an admin can view this submission
        boolean isOwnerStudent = submission.getStudent().getId().equals(user.getId());
        boolean isCourseInstructor = submission.getAssignment().getCourse().getInstructor().getId().equals(user.getId());
        boolean isAdmin = user.getRole().equals(Role.ROLE_ADMIN);

        if (!isOwnerStudent && !isCourseInstructor && !isAdmin) {
            throw new UnauthorizedException("You are not authorized to view this student's submission");
        }

        return SubmissionDto.fromEntity(submission);
    }

    private void checkCourseOwnerOrAdmin(Course course, User user) {
        if (!user.getRole().equals(Role.ROLE_ADMIN) && !course.getInstructor().getId().equals(user.getId())) {
            throw new UnauthorizedException("You do not have permission to view submissions for this course");
        }
    }
}
