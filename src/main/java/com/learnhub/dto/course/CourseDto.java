package com.learnhub.dto.course;

import com.learnhub.dto.user.UserDto;
import com.learnhub.entity.Course;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseDto {
    private Long id;
    private String title;
    private String shortDescription;
    private String category;
    private String difficulty;
    private String thumbnailUrl;
    private String estimatedDuration;
    private boolean published;
    private UserDto instructor;
    private int moduleCount;
    private int lessonCount;
    private long enrollmentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CourseDto fromEntity(Course course, long enrollmentCount) {
        int modules = course.getModules() != null ? course.getModules().size() : 0;
        int lessons = course.getModules() != null ?
                course.getModules().stream().mapToInt(m -> m.getLessons() != null ? m.getLessons().size() : 0).sum() : 0;

        return CourseDto.builder()
                .id(course.getId())
                .title(course.getTitle())
                .shortDescription(course.getShortDescription())
                .category(course.getCategory())
                .difficulty(course.getDifficulty())
                .thumbnailUrl(course.getThumbnailUrl())
                .estimatedDuration(course.getEstimatedDuration())
                .published(course.isPublished())
                .instructor(UserDto.fromEntity(course.getInstructor()))
                .moduleCount(modules)
                .lessonCount(lessons)
                .enrollmentCount(enrollmentCount)
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }
}
