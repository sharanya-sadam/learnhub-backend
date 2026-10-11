package com.learnhub.dto.lesson;

import com.learnhub.entity.Lesson;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonDto {
    private Long id;
    private Long moduleId;
    private String title;
    private String description;
    private String videoUrl;
    private String localVideoPath;
    private String duration;
    private int displayOrder;
    private String resourcePath;
    private String resourceName;
    private boolean published;
    private boolean completed; // student-specific completion status

    public static LessonDto fromEntity(Lesson lesson, boolean completed) {
        return LessonDto.builder()
                .id(lesson.getId())
                .moduleId(lesson.getModule() != null ? lesson.getModule().getId() : null)
                .title(lesson.getTitle())
                .description(lesson.getDescription())
                .videoUrl(lesson.getVideoUrl())
                .localVideoPath(lesson.getLocalVideoPath())
                .duration(lesson.getDuration())
                .displayOrder(lesson.getDisplayOrder())
                .resourcePath(lesson.getResourcePath())
                .resourceName(lesson.getResourceName())
                .published(lesson.isPublished())
                .completed(completed)
                .build();
    }
}
