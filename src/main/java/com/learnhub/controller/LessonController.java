package com.learnhub.controller;

import com.learnhub.dto.lesson.CreateLessonRequest;
import com.learnhub.dto.lesson.LessonDto;
import com.learnhub.service.LessonService;
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
@Tag(name = "Lessons", description = "Endpoints for lessons and video progress")
public class LessonController {

    private final LessonService lessonService;

    @GetMapping("/modules/{moduleId}/lessons")
    @Operation(summary = "Get all lessons in a module")
    public ResponseEntity<List<LessonDto>> getLessonsByModule(
            @PathVariable Long moduleId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails != null ? userDetails.getUsername() : null;
        return ResponseEntity.ok(lessonService.getLessonsByModuleId(moduleId, email));
    }

    @GetMapping("/lessons/{lessonId}")
    @Operation(summary = "Get lesson details by ID")
    public ResponseEntity<LessonDto> getLessonById(
            @PathVariable Long lessonId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails != null ? userDetails.getUsername() : null;
        return ResponseEntity.ok(lessonService.getLessonById(lessonId, email));
    }

    @PostMapping("/modules/{moduleId}/lessons")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Add a lesson to a module")
    public ResponseEntity<LessonDto> createLesson(
            @PathVariable Long moduleId,
            @Valid @RequestBody CreateLessonRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        LessonDto created = lessonService.createLesson(moduleId, request, userDetails.getUsername());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/lessons/{lessonId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Update an existing lesson")
    public ResponseEntity<LessonDto> updateLesson(
            @PathVariable Long lessonId,
            @Valid @RequestBody CreateLessonRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        LessonDto updated = lessonService.updateLesson(lessonId, request, userDetails.getUsername());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/lessons/{lessonId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Delete a lesson")
    public ResponseEntity<Map<String, String>> deleteLesson(
            @PathVariable Long lessonId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        lessonService.deleteLesson(lessonId, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("message", "Lesson successfully deleted"));
    }

    @PostMapping("/lessons/{lessonId}/complete")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "Toggle completed status for a lesson (Student only)")
    public ResponseEntity<LessonDto> toggleLessonCompletion(
            @PathVariable Long lessonId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        LessonDto result = lessonService.toggleLessonCompletion(lessonId, userDetails.getUsername());
        return ResponseEntity.ok(result);
    }
}
