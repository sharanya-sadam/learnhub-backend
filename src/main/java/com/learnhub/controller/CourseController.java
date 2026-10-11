package com.learnhub.controller;

import com.learnhub.dto.course.CourseDetailDto;
import com.learnhub.dto.course.CourseDto;
import com.learnhub.dto.course.CreateCourseRequest;
import com.learnhub.dto.course.UpdateCourseRequest;
import com.learnhub.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Tag(name = "Courses", description = "Endpoints for course catalog, details, and instructor management")
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    @Operation(summary = "Get published courses with search, category/difficulty filtering, sorting, and pagination")
    public ResponseEntity<Page<CourseDto>> getCourses(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String difficulty,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<CourseDto> result = courseService.getPublishedCourses(query, category, difficulty, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/categories")
    @Operation(summary = "Get list of distinct published course categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(courseService.getAllCategories());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get detailed course view including modules and lessons")
    public ResponseEntity<CourseDetailDto> getCourseDetails(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails != null ? userDetails.getUsername() : null;
        CourseDetailDto details = courseService.getCourseDetails(id, email);
        return ResponseEntity.ok(details);
    }

    @GetMapping("/my-teaching")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Get all courses taught by the logged-in instructor")
    public ResponseEntity<List<CourseDto>> getMyTeachingCourses(@AuthenticationPrincipal UserDetails userDetails) {
        List<CourseDto> courses = courseService.getInstructorCourses(userDetails.getUsername());
        return ResponseEntity.ok(courses);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Create a new course (Instructor/Admin only)")
    public ResponseEntity<CourseDto> createCourse(
            @Valid @RequestBody CreateCourseRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        CourseDto created = courseService.createCourse(request, userDetails.getUsername());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Update an existing course")
    public ResponseEntity<CourseDto> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCourseRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        CourseDto updated = courseService.updateCourse(id, request, userDetails.getUsername());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Delete a course")
    public ResponseEntity<Map<String, String>> deleteCourse(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        courseService.deleteCourse(id, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("message", "Course successfully deleted"));
    }

    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Toggle or set course publication status")
    public ResponseEntity<CourseDto> setPublishStatus(
            @PathVariable Long id,
            @RequestParam boolean published,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        CourseDto updated = courseService.setPublishStatus(id, published, userDetails.getUsername());
        return ResponseEntity.ok(updated);
    }
}
