package com.learnhub.service;

import com.learnhub.dto.admin.AdminDashboardDto;
import com.learnhub.dto.course.CourseDto;
import com.learnhub.dto.enrollment.EnrollmentDto;
import com.learnhub.dto.user.UpdateUserRequest;
import com.learnhub.dto.user.UserDto;
import com.learnhub.entity.*;
import com.learnhub.exception.BadRequestException;
import com.learnhub.exception.ResourceNotFoundException;
import com.learnhub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final LessonRepository lessonRepository;
    private final LessonProgressRepository lessonProgressRepository;

    public AdminDashboardDto getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalStudents = userRepository.countByRole(Role.ROLE_STUDENT);
        long totalInstructors = userRepository.countByRole(Role.ROLE_INSTRUCTOR);
        long totalAdmins = userRepository.countByRole(Role.ROLE_ADMIN);

        long totalCourses = courseRepository.count();
        long publishedCourses = courseRepository.countByPublished(true);
        long unpublishedCourses = totalCourses - publishedCourses;

        long totalEnrollments = enrollmentRepository.count();
        long totalAssignments = assignmentRepository.count();
        long totalSubmissions = submissionRepository.count();
        long gradedSubmissions = submissionRepository.countByStatus("GRADED");
        long pendingSubmissions = totalSubmissions - gradedSubmissions;

        Pageable recentLimit = PageRequest.of(0, 5, Sort.by("createdAt").descending());
        List<UserDto> recentUsers = userRepository.findAll(recentLimit).stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());

        List<CourseDto> recentCourses = courseRepository.findAll(recentLimit).stream()
                .map(c -> CourseDto.fromEntity(c, enrollmentRepository.countByCourse(c)))
                .collect(Collectors.toList());

        return AdminDashboardDto.builder()
                .totalUsers(totalUsers)
                .totalStudents(totalStudents)
                .totalInstructors(totalInstructors)
                .totalAdmins(totalAdmins)
                .totalCourses(totalCourses)
                .publishedCourses(publishedCourses)
                .unpublishedCourses(unpublishedCourses)
                .totalEnrollments(totalEnrollments)
                .totalAssignments(totalAssignments)
                .totalSubmissions(totalSubmissions)
                .pendingSubmissions(pendingSubmissions)
                .gradedSubmissions(gradedSubmissions)
                .recentUsers(recentUsers)
                .recentCourses(recentCourses)
                .build();
    }

    public Page<UserDto> searchUsers(String query, Role role, Pageable pageable) {
        Page<User> users = userRepository.searchUsers(query, role, pageable);
        return users.map(UserDto::fromEntity);
    }

    @Transactional
    public UserDto updateUser(Long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName().trim());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getActive() != null) {
            user.setActive(request.getActive());
        }

        user = userRepository.save(user);
        return UserDto.fromEntity(user);
    }

    @Transactional
    public UserDto setUserStatus(Long userId, boolean active, String adminEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (user.getEmail().equalsIgnoreCase(adminEmail)) {
            throw new BadRequestException("You cannot deactivate your own administrative account");
        }

        user.setActive(active);
        user = userRepository.save(user);
        return UserDto.fromEntity(user);
    }

    @Transactional
    public void deleteUser(Long userId, String adminEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (user.getEmail().equalsIgnoreCase(adminEmail)) {
            throw new BadRequestException("You cannot delete your own administrative account");
        }

        userRepository.delete(user);
    }

    public List<EnrollmentDto> getAllEnrollments() {
        List<Enrollment> enrollments = enrollmentRepository.findAll(Sort.by("enrolledAt").descending());
        return enrollments.stream().map(e -> {
            Long courseId = e.getCourse().getId();
            long totalPublished = lessonRepository.countPublishedLessonsByCourseId(courseId);
            long completed = lessonProgressRepository.countCompletedLessonsByStudentAndCourse(e.getStudent().getId(), courseId);
            double progress = totalPublished > 0 ? Math.round(((double) completed / totalPublished * 100.0) * 10.0) / 10.0 : 0.0;
            return EnrollmentDto.fromEntity(e, progress, completed, totalPublished);
        }).collect(Collectors.toList());
    }
}
