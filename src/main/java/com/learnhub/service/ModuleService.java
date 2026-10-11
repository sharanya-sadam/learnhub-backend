package com.learnhub.service;

import com.learnhub.dto.module.CreateModuleRequest;
import com.learnhub.dto.module.ModuleDto;
import com.learnhub.entity.Course;
import com.learnhub.entity.CourseModule;
import com.learnhub.entity.Role;
import com.learnhub.entity.User;
import com.learnhub.exception.ResourceNotFoundException;
import com.learnhub.exception.UnauthorizedException;
import com.learnhub.repository.CourseModuleRepository;
import com.learnhub.repository.CourseRepository;
import com.learnhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ModuleService {

    private final CourseRepository courseRepository;
    private final CourseModuleRepository moduleRepository;
    private final UserRepository userRepository;

    public List<ModuleDto> getModulesByCourseId(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));
        List<CourseModule> modules = moduleRepository.findByCourseOrderByDisplayOrderAsc(course);
        return modules.stream().map(m -> ModuleDto.fromEntity(m, Collections.emptySet())).collect(Collectors.toList());
    }

    @Transactional
    public ModuleDto createModule(Long courseId, CreateModuleRequest request, String userEmail) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        checkCourseOwnerOrAdmin(course, user);

        CourseModule module = CourseModule.builder()
                .course(course)
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .displayOrder(request.getDisplayOrder())
                .build();

        module = moduleRepository.save(module);
        return ModuleDto.fromEntity(module, Collections.emptySet());
    }

    @Transactional
    public ModuleDto updateModule(Long moduleId, CreateModuleRequest request, String userEmail) {
        CourseModule module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found with ID: " + moduleId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        checkCourseOwnerOrAdmin(module.getCourse(), user);

        module.setTitle(request.getTitle().trim());
        if (request.getDescription() != null) module.setDescription(request.getDescription());
        module.setDisplayOrder(request.getDisplayOrder());

        module = moduleRepository.save(module);
        return ModuleDto.fromEntity(module, Collections.emptySet());
    }

    @Transactional
    public void deleteModule(Long moduleId, String userEmail) {
        CourseModule module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found with ID: " + moduleId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        checkCourseOwnerOrAdmin(module.getCourse(), user);
        moduleRepository.delete(module);
    }

    private void checkCourseOwnerOrAdmin(Course course, User user) {
        if (!user.getRole().equals(Role.ROLE_ADMIN) && !course.getInstructor().getId().equals(user.getId())) {
            throw new UnauthorizedException("You do not have permission to manage modules for this course");
        }
    }
}
