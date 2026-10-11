package com.learnhub.service;

import com.learnhub.dto.course.CourseDetailDto;
import com.learnhub.dto.course.CourseDto;
import com.learnhub.dto.course.CreateCourseRequest;
import com.learnhub.dto.course.UpdateCourseRequest;
import com.learnhub.entity.*;
import com.learnhub.exception.BadRequestException;
import com.learnhub.exception.ResourceNotFoundException;
import com.learnhub.exception.UnauthorizedException;
import com.learnhub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonRepository lessonRepository;
    private final LessonProgressRepository lessonProgressRepository;

    public Page<CourseDto> getPublishedCourses(String query, String category, String difficulty, Pageable pageable) {
        Page<Course> courses = courseRepository.searchCourses(query, category, difficulty, true, pageable);
        return courses.map(course -> {
            long enrollments = enrollmentRepository.countByCourse(course);
            return CourseDto.fromEntity(course, enrollments);
        });
    }

    public Page<CourseDto> getAllCoursesForAdmin(String query, String category, String difficulty, Pageable pageable) {
        Page<Course> courses = courseRepository.searchCourses(query, category, difficulty, false, pageable);
        return courses.map(course -> {
            long enrollments = enrollmentRepository.countByCourse(course);
            return CourseDto.fromEntity(course, enrollments);
        });
    }

    public List<CourseDto> getInstructorCourses(String instructorEmail) {
        User instructor = userRepository.findByEmail(instructorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found: " + instructorEmail));
        List<Course> courses = courseRepository.findByInstructor(instructor);
        return courses.stream().map(c -> {
            long enrollments = enrollmentRepository.countByCourse(c);
            return CourseDto.fromEntity(c, enrollments);
        }).collect(Collectors.toList());
    }

    public CourseDetailDto getCourseDetails(Long courseId, String userEmail) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));

        User currentUser = userEmail != null ? userRepository.findByEmail(userEmail).orElse(null) : null;

        // If course is not published, only the course's instructor or an admin may view it
        if (!course.isPublished()) {
            if (currentUser == null || (!currentUser.getRole().equals(Role.ROLE_ADMIN) &&
                    !course.getInstructor().getId().equals(currentUser.getId()))) {
                throw new UnauthorizedException("This course is currently an unpublished draft");
            }
        }

        long enrollmentCount = enrollmentRepository.countByCourse(course);
        boolean enrolled = false;
        double progressPercentage = 0.0;
        Long lastAccessedLessonId = null;
        Set<Long> completedLessonIds = new HashSet<>();

        if (currentUser != null && currentUser.getRole().equals(Role.ROLE_STUDENT)) {
            enrolled = enrollmentRepository.existsByStudentAndCourse(currentUser, course);
            if (enrolled) {
                long totalPublished = lessonRepository.countPublishedLessonsByCourseId(courseId);
                long completedCount = lessonProgressRepository.countCompletedLessonsByStudentAndCourse(currentUser.getId(), courseId);

                if (totalPublished > 0) {
                    progressPercentage = Math.round(((double) completedCount / totalPublished * 100.0) * 10.0) / 10.0;
                }

                List<LessonProgress> progressList = lessonProgressRepository.findByStudentIdAndCourseId(currentUser.getId(), courseId);
                for (LessonProgress lp : progressList) {
                    if (lp.isCompleted()) {
                        completedLessonIds.add(lp.getLesson().getId());
                    }
                }

                List<LessonProgress> recentProgress = lessonProgressRepository.findRecentProgressByStudentAndCourse(currentUser.getId(), courseId);
                if (!recentProgress.isEmpty() && recentProgress.get(0).getLastAccessedAt() != null) {
                    lastAccessedLessonId = recentProgress.get(0).getLesson().getId();
                }
            }
        }

        return CourseDetailDto.fromEntity(course, enrollmentCount, enrolled, progressPercentage, lastAccessedLessonId, completedLessonIds);
    }

    @Transactional
    public CourseDto createCourse(CreateCourseRequest request, String instructorEmail) {
        User instructor = userRepository.findByEmail(instructorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + instructorEmail));

        if (!instructor.getRole().equals(Role.ROLE_INSTRUCTOR) && !instructor.getRole().equals(Role.ROLE_ADMIN)) {
            throw new UnauthorizedException("Only instructors or administrators can create courses");
        }

        Course course = Course.builder()
                .title(request.getTitle().trim())
                .shortDescription(request.getShortDescription())
                .description(request.getDescription())
                .category(request.getCategory())
                .difficulty(request.getDifficulty())
                .thumbnailUrl(request.getThumbnailUrl())
                .learningObjectives(request.getLearningObjectives())
                .prerequisites(request.getPrerequisites())
                .estimatedDuration(request.getEstimatedDuration())
                .published(request.isPublished())
                .instructor(instructor)
                .build();

        course = courseRepository.save(course);
        return CourseDto.fromEntity(course, 0);
    }

    @Transactional
    public CourseDto updateCourse(Long courseId, UpdateCourseRequest request, String userEmail) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        checkCourseModificationPermission(course, user);

        if (request.getTitle() != null && !request.getTitle().isBlank()) course.setTitle(request.getTitle().trim());
        if (request.getShortDescription() != null) course.setShortDescription(request.getShortDescription());
        if (request.getDescription() != null) course.setDescription(request.getDescription());
        if (request.getCategory() != null) course.setCategory(request.getCategory());
        if (request.getDifficulty() != null) course.setDifficulty(request.getDifficulty());
        if (request.getThumbnailUrl() != null) course.setThumbnailUrl(request.getThumbnailUrl());
        if (request.getLearningObjectives() != null) course.setLearningObjectives(request.getLearningObjectives());
        if (request.getPrerequisites() != null) course.setPrerequisites(request.getPrerequisites());
        if (request.getEstimatedDuration() != null) course.setEstimatedDuration(request.getEstimatedDuration());
        if (request.getPublished() != null) course.setPublished(request.getPublished());

        course = courseRepository.save(course);
        long enrollments = enrollmentRepository.countByCourse(course);
        return CourseDto.fromEntity(course, enrollments);
    }

    @Transactional
    public void deleteCourse(Long courseId, String userEmail) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        checkCourseModificationPermission(course, user);
        courseRepository.delete(course);
    }

    @Transactional
    public CourseDto setPublishStatus(Long courseId, boolean published, String userEmail) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        checkCourseModificationPermission(course, user);
        course.setPublished(published);
        course = courseRepository.save(course);

        long enrollments = enrollmentRepository.countByCourse(course);
        return CourseDto.fromEntity(course, enrollments);
    }

    public List<String> getAllCategories() {
        return courseRepository.findDistinctPublishedCategories();
    }

    private void checkCourseModificationPermission(Course course, User user) {
        if (!user.getRole().equals(Role.ROLE_ADMIN) && !course.getInstructor().getId().equals(user.getId())) {
            throw new UnauthorizedException("You do not have permission to modify this course");
        }
    }
}
