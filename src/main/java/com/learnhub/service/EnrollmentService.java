package com.learnhub.service;

import com.learnhub.dto.enrollment.EnrollmentDto;
import com.learnhub.dto.user.UserDto;
import com.learnhub.entity.*;
import com.learnhub.exception.BadRequestException;
import com.learnhub.exception.ResourceNotFoundException;
import com.learnhub.exception.UnauthorizedException;
import com.learnhub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;
    private final LessonProgressRepository lessonProgressRepository;

    @Transactional
    public EnrollmentDto enrollInCourse(Long courseId, String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentEmail));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));

        if (!course.isPublished()) {
            throw new BadRequestException("Cannot enroll in an unpublished course");
        }

        if (enrollmentRepository.existsByStudentAndCourse(student, course)) {
            throw new BadRequestException("You are already enrolled in this course");
        }

        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .status("ACTIVE")
                .build();

        enrollment = enrollmentRepository.save(enrollment);

        long totalPublished = lessonRepository.countPublishedLessonsByCourseId(courseId);
        long completed = lessonProgressRepository.countCompletedLessonsByStudentAndCourse(student.getId(), courseId);
        double progress = totalPublished > 0 ? ((double) completed / totalPublished) * 100.0 : 0.0;

        return EnrollmentDto.fromEntity(enrollment, progress, completed, totalPublished);
    }

    public List<EnrollmentDto> getMyEnrollments(String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentEmail));

        List<Enrollment> enrollments = enrollmentRepository.findByStudentOrderByEnrolledAtDesc(student);

        return enrollments.stream().map(e -> {
            Long courseId = e.getCourse().getId();
            long totalPublished = lessonRepository.countPublishedLessonsByCourseId(courseId);
            long completed = lessonProgressRepository.countCompletedLessonsByStudentAndCourse(student.getId(), courseId);
            double progress = totalPublished > 0 ? Math.round(((double) completed / totalPublished * 100.0) * 10.0) / 10.0 : 0.0;
            return EnrollmentDto.fromEntity(e, progress, completed, totalPublished);
        }).collect(Collectors.toList());
    }

    public List<UserDto> getEnrolledStudents(Long courseId, String userEmail) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        if (!user.getRole().equals(Role.ROLE_ADMIN) && !course.getInstructor().getId().equals(user.getId())) {
            throw new UnauthorizedException("You do not have permission to view enrolled students for this course");
        }

        List<Enrollment> enrollments = enrollmentRepository.findByCourseOrderByEnrolledAtDesc(course);
        return enrollments.stream()
                .map(e -> UserDto.fromEntity(e.getStudent()))
                .collect(Collectors.toList());
    }
}
