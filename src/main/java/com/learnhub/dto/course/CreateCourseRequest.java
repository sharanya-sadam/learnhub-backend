package com.learnhub.dto.course;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCourseRequest {

    @NotBlank(message = "Course title is required")
    @Size(min = 3, max = 255, message = "Course title must be between 3 and 255 characters")
    private String title;

    @Size(max = 500, message = "Short description must not exceed 500 characters")
    private String shortDescription;

    private String description;

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Difficulty level is required")
    private String difficulty; // Beginner, Intermediate, Advanced

    private String thumbnailUrl;
    private String learningObjectives;
    private String prerequisites;
    private String estimatedDuration;
    private boolean published;
}
