package com.learnhub.dto.enrollment;

import com.learnhub.dto.course.CourseDto;
import com.learnhub.dto.user.UserDto;
import com.learnhub.entity.Enrollment;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentDto {
    private Long id;
    private Long courseId;
    private String courseTitle;
    private String courseThumbnail;
    private CourseDto course;
    private UserDto student;
    private LocalDateTime enrolledAt;
    private String status;
    private double progressPercentage;
    private long completedLessons;
    private long totalLessons;

    public static EnrollmentDto fromEntity(Enrollment enrollment, double progressPercentage, long completedLessons, long totalLessons) {
        return EnrollmentDto.builder()
                .id(enrollment.getId())
                .courseId(enrollment.getCourse().getId())
                .courseTitle(enrollment.getCourse().getTitle())
                .courseThumbnail(enrollment.getCourse().getThumbnailUrl())
                .course(CourseDto.fromEntity(enrollment.getCourse(), 0))
                .student(UserDto.fromEntity(enrollment.getStudent()))
                .enrolledAt(enrollment.getEnrolledAt())
                .status(enrollment.getStatus())
                .progressPercentage(progressPercentage)
                .completedLessons(completedLessons)
                .totalLessons(totalLessons)
                .build();
    }
}
