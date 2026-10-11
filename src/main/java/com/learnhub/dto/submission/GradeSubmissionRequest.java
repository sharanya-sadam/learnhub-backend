package com.learnhub.dto.submission;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeSubmissionRequest {

    @NotNull(message = "Marks awarded is required")
    @DecimalMin(value = "0.0", message = "Marks cannot be negative")
    private BigDecimal marksAwarded;

    private String feedback;
}
