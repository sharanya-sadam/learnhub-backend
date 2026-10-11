package com.learnhub.dto.assignment;

import com.learnhub.entity.Assignment;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentDto {
    private Long id;
    private Long courseId;
    private String courseTitle;
    private String title;
    private String description;
    private String instructions;
    private BigDecimal maxMarks;
    private LocalDateTime dueDate;
    private String allowedSubmissionType;
    private boolean allowLateSubmissions;
    private String attachmentPath;
    private String attachmentName;
    private boolean published;
    private LocalDateTime createdAt;
    private boolean isSubmitted;
    private String submissionStatus;
    private BigDecimal studentMarks;
    private String studentFeedback;
    private boolean isOverdue;

    public static AssignmentDto fromEntity(Assignment assignment, boolean isSubmitted, String submissionStatus, BigDecimal marks, String feedback) {
        boolean overdue = LocalDateTime.now().isAfter(assignment.getDueDate());
        return AssignmentDto.builder()
                .id(assignment.getId())
                .courseId(assignment.getCourse().getId())
                .courseTitle(assignment.getCourse().getTitle())
                .title(assignment.getTitle())
                .description(assignment.getDescription())
                .instructions(assignment.getInstructions())
                .maxMarks(assignment.getMaxMarks())
                .dueDate(assignment.getDueDate())
                .allowedSubmissionType(assignment.getAllowedSubmissionType())
                .allowLateSubmissions(assignment.isAllowLateSubmissions())
                .attachmentPath(assignment.getAttachmentPath())
                .attachmentName(assignment.getAttachmentName())
                .published(assignment.isPublished())
                .createdAt(assignment.getCreatedAt())
                .isSubmitted(isSubmitted)
                .submissionStatus(submissionStatus)
                .studentMarks(marks)
                .studentFeedback(feedback)
                .isOverdue(overdue)
                .build();
    }
}
