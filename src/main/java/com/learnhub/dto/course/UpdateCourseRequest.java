package com.learnhub.dto.course;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCourseRequest {
    private String title;
    private String shortDescription;
    private String description;
    private String category;
    private String difficulty;
    private String thumbnailUrl;
    private String learningObjectives;
    private String prerequisites;
    private String estimatedDuration;
    private Boolean published;
}
