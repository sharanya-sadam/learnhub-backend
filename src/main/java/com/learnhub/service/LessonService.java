package com.learnhub.service;

import com.learnhub.dto.lesson.CreateLessonRequest;
import com.learnhub.dto.lesson.LessonDto;
import com.learnhub.entity.*;
import com.learnhub.exception.ResourceNotFoundException;
import com.learnhub.exception.UnauthorizedException;
import com.learnhub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final CourseModuleRepository moduleRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonProgressRepository lessonProgressRepository;

    public List<LessonDto> getLessonsByModuleId(Long moduleId, String userEmail) {
        CourseModule module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found with ID: " + moduleId));

        User currentUser = userEmail != null ? userRepository.findByEmail(userEmail).orElse(null) : null;
        Long currentUserId = currentUser != null ? currentUser.getId() : null;

        List<Lesson> lessons = lessonRepository.findByModuleOrderByDisplayOrderAsc(module);
        return lessons.stream().map(l -> {
            boolean completed = false;
            if (currentUserId != null) {
                completed = lessonProgressRepository.findByStudentIdAndLessonId(currentUserId, l.getId())
                        .map(LessonProgress::isCompleted)
                        .orElse(false);
            }
            return LessonDto.fromEntity(l, completed);
        }).collect(Collectors.toList());
    }

    public LessonDto getLessonById(Long lessonId, String userEmail) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with ID: " + lessonId));

        User currentUser = userEmail != null ? userRepository.findByEmail(userEmail).orElse(null) : null;
        boolean completed = false;

        if (currentUser != null) {
            // Update last accessed time in lesson progress
            Optional<LessonProgress> lpOpt = lessonProgressRepository.findByStudentIdAndLessonId(currentUser.getId(), lessonId);
            LessonProgress lp = lpOpt.orElseGet(() -> LessonProgress.builder()
                    .student(currentUser)
                    .lesson(lesson)
                    .completed(false)
                    .build());
            lp.setLastAccessedAt(LocalDateTime.now());
            lessonProgressRepository.save(lp);
            completed = lp.isCompleted();
        }

        return LessonDto.fromEntity(lesson, completed);
    }

    @Transactional
    public LessonDto createLesson(Long moduleId, CreateLessonRequest request, String userEmail) {
        CourseModule module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found with ID: " + moduleId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        checkCourseOwnerOrAdmin(module.getCourse(), user);

        Lesson lesson = Lesson.builder()
                .module(module)
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .videoUrl(request.getVideoUrl())
                .localVideoPath(request.getLocalVideoPath())
                .duration(request.getDuration())
                .displayOrder(request.getDisplayOrder())
                .resourcePath(request.getResourcePath())
                .resourceName(request.getResourceName())
                .published(request.isPublished())
                .build();

        lesson = lessonRepository.save(lesson);
        return LessonDto.fromEntity(lesson, false);
    }

    @Transactional
    public LessonDto updateLesson(Long lessonId, CreateLessonRequest request, String userEmail) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with ID: " + lessonId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        checkCourseOwnerOrAdmin(lesson.getModule().getCourse(), user);

        lesson.setTitle(request.getTitle().trim());
        if (request.getDescription() != null) lesson.setDescription(request.getDescription());
        if (request.getVideoUrl() != null) lesson.setVideoUrl(request.getVideoUrl());
        if (request.getLocalVideoPath() != null) lesson.setLocalVideoPath(request.getLocalVideoPath());
        if (request.getDuration() != null) lesson.setDuration(request.getDuration());
        lesson.setDisplayOrder(request.getDisplayOrder());
        if (request.getResourcePath() != null) lesson.setResourcePath(request.getResourcePath());
        if (request.getResourceName() != null) lesson.setResourceName(request.getResourceName());
        lesson.setPublished(request.isPublished());

        lesson = lessonRepository.save(lesson);
        return LessonDto.fromEntity(lesson, false);
    }

    @Transactional
    public void deleteLesson(Long lessonId, String userEmail) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with ID: " + lessonId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        checkCourseOwnerOrAdmin(lesson.getModule().getCourse(), user);
        lessonRepository.delete(lesson);
    }

    @Transactional
    public LessonDto toggleLessonCompletion(Long lessonId, String userEmail) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with ID: " + lessonId));

        User student = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        // Ensure student is enrolled in the course
        boolean isEnrolled = enrollmentRepository.existsByStudentAndCourse(student, lesson.getModule().getCourse());
        if (!isEnrolled && !student.getRole().equals(Role.ROLE_ADMIN)) {
            throw new UnauthorizedException("You must be enrolled in the course to track lesson progress");
        }

        Optional<LessonProgress> lpOpt = lessonProgressRepository.findByStudentIdAndLessonId(student.getId(), lessonId);
        LessonProgress lp;
        if (lpOpt.isPresent()) {
            lp = lpOpt.get();
            lp.setCompleted(!lp.isCompleted());
            if (lp.isCompleted()) {
                lp.setCompletedAt(LocalDateTime.now());
            } else {
                lp.setCompletedAt(null);
            }
            lp.setLastAccessedAt(LocalDateTime.now());
        } else {
            lp = LessonProgress.builder()
                    .student(student)
                    .lesson(lesson)
                    .completed(true)
                    .completedAt(LocalDateTime.now())
                    .lastAccessedAt(LocalDateTime.now())
                    .build();
        }

        lp = lessonProgressRepository.save(lp);
        return LessonDto.fromEntity(lesson, lp.isCompleted());
    }

    private void checkCourseOwnerOrAdmin(Course course, User user) {
        if (!user.getRole().equals(Role.ROLE_ADMIN) && !course.getInstructor().getId().equals(user.getId())) {
            throw new UnauthorizedException("You do not have permission to manage lessons for this course");
        }
    }
}
