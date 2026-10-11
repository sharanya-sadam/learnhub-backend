package com.learnhub.dto.assignment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAssignmentRequest {

    @NotBlank(message = "Assignment title is required")
    private String title;

    private String description;
    private String instructions;

    @NotNull(message = "Maximum marks is required")
    @DecimalMin(value = "1.0", message = "Maximum marks must be at least 1.0")
    private BigDecimal maxMarks;

    @NotNull(message = "Due date is required")
    private LocalDateTime dueDate;

    @NotBlank(message = "Allowed submission type is required")
    private String allowedSubmissionType; // "TEXT", "FILE", "BOTH"

    @Builder.Default
    private boolean allowLateSubmissions = true;
    private String attachmentPath;
    private String attachmentName;
    @Builder.Default
    private boolean published = true;
}
