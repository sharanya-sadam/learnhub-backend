package com.learnhub.controller;

import com.learnhub.dto.submission.CreateSubmissionRequest;
import com.learnhub.dto.submission.SubmissionDto;
import com.learnhub.service.SubmissionService;
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
@Tag(name = "Submissions", description = "Endpoints for submitting assignments and viewing submissions")
public class SubmissionController {

    private final SubmissionService submissionService;

    @PostMapping("/assignments/{assignmentId}/submissions")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "Submit assignment (Student only)")
    public ResponseEntity<SubmissionDto> submitAssignment(
            @PathVariable Long assignmentId,
            @RequestBody CreateSubmissionRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        SubmissionDto submission = submissionService.submitAssignment(assignmentId, request, userDetails.getUsername());
        return new ResponseEntity<>(submission, HttpStatus.CREATED);
    }

    @GetMapping("/assignments/{assignmentId}/my-submission")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "Get the authenticated student's submission for an assignment")
    public ResponseEntity<SubmissionDto> getMySubmission(
            @PathVariable Long assignmentId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(submissionService.getMySubmission(assignmentId, userDetails.getUsername()));
    }

    @GetMapping("/assignments/{assignmentId}/submissions")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Get all submissions for an assignment (Instructor/Admin only)")
    public ResponseEntity<List<SubmissionDto>> getSubmissionsForAssignment(
            @PathVariable Long assignmentId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(submissionService.getSubmissionsForAssignment(assignmentId, userDetails.getUsername()));
    }

    @GetMapping("/submissions/{submissionId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Get submission by ID with strict ownership authorization")
    public ResponseEntity<SubmissionDto> getSubmissionById(
            @PathVariable Long submissionId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(submissionService.getSubmissionById(submissionId, userDetails.getUsername()));
    }
}
