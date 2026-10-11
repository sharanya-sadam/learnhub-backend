package com.learnhub.controller;

import com.learnhub.dto.assignment.AssignmentDto;
import com.learnhub.dto.assignment.CreateAssignmentRequest;
import com.learnhub.service.AssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Assignments", description = "Endpoints for course assignments creation and student viewing")
public class AssignmentController {

    private final AssignmentService assignmentService;

    @GetMapping("/courses/{courseId}/assignments")
    @Operation(summary = "Get all assignments for a course")
    public ResponseEntity<List<AssignmentDto>> getAssignmentsByCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails != null ? userDetails.getUsername() : null;
        return ResponseEntity.ok(assignmentService.getAssignmentsByCourseId(courseId, email));
    }

    @GetMapping("/assignments/{assignmentId}")
    @Operation(summary = "Get assignment details by ID")
    public ResponseEntity<AssignmentDto> getAssignmentById(
            @PathVariable Long assignmentId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails != null ? userDetails.getUsername() : null;
        return ResponseEntity.ok(assignmentService.getAssignmentById(assignmentId, email));
    }

    @PostMapping("/courses/{courseId}/assignments")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Create an assignment for a course (Instructor/Admin only)")
    public ResponseEntity<AssignmentDto> createAssignment(
            @PathVariable Long courseId,
            @Valid @RequestBody CreateAssignmentRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        AssignmentDto created = assignmentService.createAssignment(courseId, request, userDetails.getUsername());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/assignments/{assignmentId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Update an assignment")
    public ResponseEntity<AssignmentDto> updateAssignment(
            @PathVariable Long assignmentId,
            @Valid @RequestBody CreateAssignmentRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        AssignmentDto updated = assignmentService.updateAssignment(assignmentId, request, userDetails.getUsername());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/assignments/{assignmentId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Delete an assignment")
    public ResponseEntity<Map<String, String>> deleteAssignment(
            @PathVariable Long assignmentId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        assignmentService.deleteAssignment(assignmentId, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("message", "Assignment successfully deleted"));
    }
}
