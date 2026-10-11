package com.learnhub.dto.lesson;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateLessonRequest {

    @NotBlank(message = "Lesson title is required")
    private String title;

    private String description;
    private String videoUrl;
    private String localVideoPath;
    private String duration;
    @Builder.Default
    private int displayOrder = 1;
    private String resourcePath;
    private String resourceName;
    @Builder.Default
    private boolean published = true;
}
