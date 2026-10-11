package com.learnhub.dto.submission;

import com.learnhub.dto.user.UserDto;
import com.learnhub.entity.Submission;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionDto {
    private Long id;
    private Long assignmentId;
    private String assignmentTitle;
    private BigDecimal maxMarks;
    private Long courseId;
    private String courseTitle;
    private UserDto student;
    private String textResponse;
    private String filePath;
    private String originalFileName;
    private LocalDateTime submittedAt;
    private String status; // SUBMITTED, LATE, GRADED
    private BigDecimal marksAwarded;
    private String feedback;
    private LocalDateTime gradedAt;
    private UserDto gradedBy;
    private int submissionVersion;
    private LocalDateTime updatedAt;

    public static SubmissionDto fromEntity(Submission submission) {
        return SubmissionDto.builder()
                .id(submission.getId())
                .assignmentId(submission.getAssignment().getId())
                .assignmentTitle(submission.getAssignment().getTitle())
                .maxMarks(submission.getAssignment().getMaxMarks())
                .courseId(submission.getAssignment().getCourse().getId())
                .courseTitle(submission.getAssignment().getCourse().getTitle())
                .student(UserDto.fromEntity(submission.getStudent()))
                .textResponse(submission.getTextResponse())
                .filePath(submission.getFilePath())
                .originalFileName(submission.getOriginalFileName())
                .submittedAt(submission.getSubmittedAt())
                .status(submission.getStatus())
                .marksAwarded(submission.getMarksAwarded())
                .feedback(submission.getFeedback())
                .gradedAt(submission.getGradedAt())
                .gradedBy(UserDto.fromEntity(submission.getGradedBy()))
                .submissionVersion(submission.getSubmissionVersion())
                .updatedAt(submission.getUpdatedAt())
                .build();
    }
}
