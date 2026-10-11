package com.learnhub.dto.course;

import com.learnhub.dto.module.ModuleDto;
import com.learnhub.dto.user.UserDto;
import com.learnhub.entity.Course;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseDetailDto {
    private Long id;
    private String title;
    private String shortDescription;
    private String description;
    private String category;
    private String difficulty;
    private String thumbnailUrl;
    private String learningObjectives;
    private String prerequisites;
    private String estimatedDuration;
    private boolean published;
    private UserDto instructor;
    private long enrollmentCount;
    private int moduleCount;
    private int lessonCount;
    private boolean enrolled;
    private double progressPercentage;
    private Long lastAccessedLessonId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @Builder.Default
    private List<ModuleDto> modules = new ArrayList<>();

    public static CourseDetailDto fromEntity(Course course, long enrollmentCount, boolean enrolled, double progressPercentage, Long lastAccessedLessonId, Set<Long> completedLessonIds) {
        List<ModuleDto> moduleDtos = new ArrayList<>();
        int lessons = 0;
        if (course.getModules() != null) {
            for (var m : course.getModules()) {
                moduleDtos.add(ModuleDto.fromEntity(m, completedLessonIds));
                if (m.getLessons() != null) {
                    lessons += m.getLessons().size();
                }
            }
        }

        return CourseDetailDto.builder()
                .id(course.getId())
                .title(course.getTitle())
                .shortDescription(course.getShortDescription())
                .description(course.getDescription())
                .category(course.getCategory())
                .difficulty(course.getDifficulty())
                .thumbnailUrl(course.getThumbnailUrl())
                .learningObjectives(course.getLearningObjectives())
                .prerequisites(course.getPrerequisites())
                .estimatedDuration(course.getEstimatedDuration())
                .published(course.isPublished())
                .instructor(UserDto.fromEntity(course.getInstructor()))
                .enrollmentCount(enrollmentCount)
                .moduleCount(moduleDtos.size())
                .lessonCount(lessons)
                .enrolled(enrolled)
                .progressPercentage(progressPercentage)
                .lastAccessedLessonId(lastAccessedLessonId)
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .modules(moduleDtos)
                .build();
    }
}
