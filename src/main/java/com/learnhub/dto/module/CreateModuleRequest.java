package com.learnhub.dto.module;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateModuleRequest {

    @NotBlank(message = "Module title is required")
    private String title;

    private String description;
    @Builder.Default
    private int displayOrder = 1;
}
