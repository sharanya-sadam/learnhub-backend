package com.learnhub.controller;

import com.learnhub.dto.submission.GradeSubmissionRequest;
import com.learnhub.dto.submission.GradebookEntryDto;
import com.learnhub.dto.submission.SubmissionDto;
import com.learnhub.service.GradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Grading", description = "Endpoints for grading submissions and accessing student gradebooks")
public class GradeController {

    private final GradeService gradeService;

    @PostMapping("/submissions/{submissionId}/grade")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Grade student submission (Instructor/Admin only)")
    public ResponseEntity<SubmissionDto> gradeSubmission(
            @PathVariable Long submissionId,
            @Valid @RequestBody GradeSubmissionRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        SubmissionDto graded = gradeService.gradeSubmission(submissionId, request, userDetails.getUsername());
        return ResponseEntity.ok(graded);
    }

    @GetMapping("/grades/my")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "Get the authenticated student's gradebook across all enrolled courses")
    public ResponseEntity<List<GradebookEntryDto>> getMyGradebook(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(gradeService.getStudentGradebook(userDetails.getUsername()));
    }

    @GetMapping("/courses/{courseId}/gradebook")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Get all submissions and grades for a course (Instructor/Admin only)")
    public ResponseEntity<List<SubmissionDto>> getCourseGradebook(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(gradeService.getCourseSubmissions(courseId, userDetails.getUsername()));
    }
}
