package com.learnhub.service;

import com.learnhub.dto.assignment.AssignmentDto;
import com.learnhub.dto.assignment.CreateAssignmentRequest;
import com.learnhub.entity.*;
import com.learnhub.exception.BadRequestException;
import com.learnhub.exception.ResourceNotFoundException;
import com.learnhub.exception.UnauthorizedException;
import com.learnhub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final SubmissionRepository submissionRepository;

    public List<AssignmentDto> getAssignmentsByCourseId(Long courseId, String userEmail) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));

        User currentUser = userEmail != null ? userRepository.findByEmail(userEmail).orElse(null) : null;

        if (currentUser != null && currentUser.getRole().equals(Role.ROLE_STUDENT)) {
            boolean enrolled = enrollmentRepository.existsByStudentAndCourse(currentUser, course);
            if (!enrolled) {
                throw new UnauthorizedException("You must be enrolled in the course to view assignments");
            }
        }

        List<Assignment> assignments = assignmentRepository.findByCourseIdOrderByDueDateAsc(courseId);

        return assignments.stream().map(a -> {
            boolean isSubmitted = false;
            String status = null;
            java.math.BigDecimal marks = null;
            String feedback = null;

            if (currentUser != null && currentUser.getRole().equals(Role.ROLE_STUDENT)) {
                Optional<Submission> subOpt = submissionRepository.findByAssignmentIdAndStudentId(a.getId(), currentUser.getId());
                if (subOpt.isPresent()) {
                    Submission s = subOpt.get();
                    isSubmitted = true;
                    status = s.getStatus();
                    marks = s.getMarksAwarded();
                    feedback = s.getFeedback();
                }
            }

            return AssignmentDto.fromEntity(a, isSubmitted, status, marks, feedback);
        }).collect(Collectors.toList());
    }

    public AssignmentDto getAssignmentById(Long assignmentId, String userEmail) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with ID: " + assignmentId));

        User currentUser = userEmail != null ? userRepository.findByEmail(userEmail).orElse(null) : null;

        boolean isSubmitted = false;
        String status = null;
        java.math.BigDecimal marks = null;
        String feedback = null;

        if (currentUser != null && currentUser.getRole().equals(Role.ROLE_STUDENT)) {
            boolean enrolled = enrollmentRepository.existsByStudentAndCourse(currentUser, assignment.getCourse());
            if (!enrolled) {
                throw new UnauthorizedException("You must be enrolled in the course to view this assignment");
            }

            Optional<Submission> subOpt = submissionRepository.findByAssignmentIdAndStudentId(assignmentId, currentUser.getId());
            if (subOpt.isPresent()) {
                Submission s = subOpt.get();
                isSubmitted = true;
                status = s.getStatus();
                marks = s.getMarksAwarded();
                feedback = s.getFeedback();
            }
        }

        return AssignmentDto.fromEntity(assignment, isSubmitted, status, marks, feedback);
    }

    @Transactional
    public AssignmentDto createAssignment(Long courseId, CreateAssignmentRequest request, String userEmail) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        checkCourseOwnerOrAdmin(course, user);

        Assignment assignment = Assignment.builder()
                .course(course)
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .instructions(request.getInstructions())
                .maxMarks(request.getMaxMarks())
                .dueDate(request.getDueDate())
                .allowedSubmissionType(request.getAllowedSubmissionType())
                .allowLateSubmissions(request.isAllowLateSubmissions())
                .attachmentPath(request.getAttachmentPath())
                .attachmentName(request.getAttachmentName())
                .published(request.isPublished())
                .build();

        assignment = assignmentRepository.save(assignment);
        return AssignmentDto.fromEntity(assignment, false, null, null, null);
    }

    @Transactional
    public AssignmentDto updateAssignment(Long assignmentId, CreateAssignmentRequest request, String userEmail) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with ID: " + assignmentId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        checkCourseOwnerOrAdmin(assignment.getCourse(), user);

        assignment.setTitle(request.getTitle().trim());
        if (request.getDescription() != null) assignment.setDescription(request.getDescription());
        if (request.getInstructions() != null) assignment.setInstructions(request.getInstructions());
        assignment.setMaxMarks(request.getMaxMarks());
        assignment.setDueDate(request.getDueDate());
        assignment.setAllowedSubmissionType(request.getAllowedSubmissionType());
        assignment.setAllowLateSubmissions(request.isAllowLateSubmissions());
        if (request.getAttachmentPath() != null) assignment.setAttachmentPath(request.getAttachmentPath());
        if (request.getAttachmentName() != null) assignment.setAttachmentName(request.getAttachmentName());
        assignment.setPublished(request.isPublished());

        assignment = assignmentRepository.save(assignment);
        return AssignmentDto.fromEntity(assignment, false, null, null, null);
    }

    @Transactional
    public void deleteAssignment(Long assignmentId, String userEmail) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with ID: " + assignmentId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        checkCourseOwnerOrAdmin(assignment.getCourse(), user);
        assignmentRepository.delete(assignment);
    }

    private void checkCourseOwnerOrAdmin(Course course, User user) {
        if (!user.getRole().equals(Role.ROLE_ADMIN) && !course.getInstructor().getId().equals(user.getId())) {
            throw new UnauthorizedException("You do not have permission to manage assignments for this course");
        }
    }
}
