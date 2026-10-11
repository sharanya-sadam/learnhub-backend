package com.learnhub.controller;

import com.learnhub.dto.admin.AdminDashboardDto;
import com.learnhub.dto.course.CourseDto;
import com.learnhub.dto.enrollment.EnrollmentDto;
import com.learnhub.dto.user.UpdateUserRequest;
import com.learnhub.dto.user.UserDto;
import com.learnhub.entity.Role;
import com.learnhub.service.AdminService;
import com.learnhub.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Endpoints for platform administration, user management, and global metrics")
public class AdminController {

    private final AdminService adminService;
    private final CourseService courseService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get platform-wide statistics for the admin dashboard")
    public ResponseEntity<AdminDashboardDto> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/users")
    @Operation(summary = "Search, filter, and paginate all platform users")
    public ResponseEntity<Page<UserDto>> getUsers(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Role role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(adminService.searchUsers(query, role, pageable));
    }

    @PutMapping("/users/{id}")
    @Operation(summary = "Update user details or role")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable Long id,
            @RequestBody UpdateUserRequest request
    ) {
        return ResponseEntity.ok(adminService.updateUser(id, request));
    }

    @PatchMapping("/users/{id}/status")
    @Operation(summary = "Activate or deactivate a user account")
    public ResponseEntity<UserDto> setUserStatus(
            @PathVariable Long id,
            @RequestParam boolean active,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(adminService.setUserStatus(id, active, userDetails.getUsername()));
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Delete a user account")
    public ResponseEntity<Map<String, String>> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        adminService.deleteUser(id, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("message", "User successfully deleted"));
    }

    @GetMapping("/courses")
    @Operation(summary = "Get all platform courses including drafts")
    public ResponseEntity<Page<CourseDto>> getAllCourses(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String difficulty,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(courseService.getAllCoursesForAdmin(query, category, difficulty, pageable));
    }

    @GetMapping("/enrollments")
    @Operation(summary = "Get all platform enrollments")
    public ResponseEntity<List<EnrollmentDto>> getAllEnrollments() {
        return ResponseEntity.ok(adminService.getAllEnrollments());
    }
}
