package com.learnhub.dto.submission;

import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradebookEntryDto {
    private Long assignmentId;
    private Long courseId;
    private String courseName;
    private String assignmentTitle;
    private BigDecimal maxMarks;
    private BigDecimal marksObtained;
    private BigDecimal percentage;
    private String submissionStatus; // NOT_SUBMITTED, SUBMITTED, LATE
    private String gradingStatus; // NOT_SUBMITTED, PENDING_GRADING, GRADED
    private String feedback;
    private LocalDateTime gradingDate;
    private LocalDateTime submittedAt;
    private LocalDateTime dueDate;

    public static GradebookEntryDto create(
            Long assignmentId,
            Long courseId,
            String courseName,
            String assignmentTitle,
            BigDecimal maxMarks,
            BigDecimal marksObtained,
            String submissionStatus,
            String gradingStatus,
            String feedback,
            LocalDateTime gradingDate,
            LocalDateTime submittedAt,
            LocalDateTime dueDate
    ) {
        BigDecimal pct = null;
        if (marksObtained != null && maxMarks != null && maxMarks.compareTo(BigDecimal.ZERO) > 0) {
            pct = marksObtained.multiply(BigDecimal.valueOf(100))
                    .divide(maxMarks, 2, RoundingMode.HALF_UP);
        }

        return GradebookEntryDto.builder()
                .assignmentId(assignmentId)
                .courseId(courseId)
                .courseName(courseName)
                .assignmentTitle(assignmentTitle)
                .maxMarks(maxMarks)
                .marksObtained(marksObtained)
                .percentage(pct)
                .submissionStatus(submissionStatus)
                .gradingStatus(gradingStatus)
                .feedback(feedback)
                .gradingDate(gradingDate)
                .submittedAt(submittedAt)
                .dueDate(dueDate)
                .build();
    }
}
