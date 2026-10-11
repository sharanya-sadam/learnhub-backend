package com.learnhub.controller;

import com.learnhub.dto.enrollment.EnrollmentDto;
import com.learnhub.dto.user.UserDto;
import com.learnhub.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Enrollments", description = "Endpoints for course enrollment and student progress lists")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping("/courses/{courseId}/enroll")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "Enroll current student into a course")
    public ResponseEntity<EnrollmentDto> enroll(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        EnrollmentDto enrolled = enrollmentService.enrollInCourse(courseId, userDetails.getUsername());
        return new ResponseEntity<>(enrolled, HttpStatus.CREATED);
    }

    @GetMapping("/enrollments/my")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "Get enrolled courses with progress for the logged-in student")
    public ResponseEntity<List<EnrollmentDto>> getMyEnrollments(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(enrollmentService.getMyEnrollments(userDetails.getUsername()));
    }

    @GetMapping("/courses/{courseId}/students")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Get all students enrolled in a specific course")
    public ResponseEntity<List<UserDto>> getCourseStudents(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(enrollmentService.getEnrolledStudents(courseId, userDetails.getUsername()));
    }
}
