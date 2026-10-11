package com.learnhub.controller;

import com.learnhub.dto.module.CreateModuleRequest;
import com.learnhub.dto.module.ModuleDto;
import com.learnhub.service.ModuleService;
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
@Tag(name = "Modules", description = "Endpoints for course modules management")
public class ModuleController {

    private final ModuleService moduleService;

    @GetMapping("/courses/{courseId}/modules")
    @Operation(summary = "Get all modules for a course")
    public ResponseEntity<List<ModuleDto>> getModulesByCourseId(@PathVariable Long courseId) {
        return ResponseEntity.ok(moduleService.getModulesByCourseId(courseId));
    }

    @PostMapping("/courses/{courseId}/modules")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Add a module to a course")
    public ResponseEntity<ModuleDto> createModule(
            @PathVariable Long courseId,
            @Valid @RequestBody CreateModuleRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        ModuleDto created = moduleService.createModule(courseId, request, userDetails.getUsername());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/modules/{moduleId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Update an existing module")
    public ResponseEntity<ModuleDto> updateModule(
            @PathVariable Long moduleId,
            @Valid @RequestBody CreateModuleRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        ModuleDto updated = moduleService.updateModule(moduleId, request, userDetails.getUsername());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/modules/{moduleId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Delete a module")
    public ResponseEntity<Map<String, String>> deleteModule(
            @PathVariable Long moduleId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        moduleService.deleteModule(moduleId, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("message", "Module successfully deleted"));
    }
}
