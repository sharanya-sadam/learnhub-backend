package com.learnhub.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "submissions", uniqueConstraints = {
    @UniqueConstraint(name = "uk_student_assignment", columnNames = {"student_id", "assignment_id"})
}, indexes = {
    @Index(name = "idx_submission_assignment", columnList = "assignment_id"),
    @Index(name = "idx_submission_student", columnList = "student_id"),
    @Index(name = "idx_submission_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "submissions"})
    private Assignment assignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "passwordHash"})
    private User student;

    @Column(columnDefinition = "LONGTEXT")
    private String textResponse;

    private String filePath;

    private String originalFileName;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime submittedAt;

    @Column(nullable = false)
    @Builder.Default
    private String status = "SUBMITTED"; // "SUBMITTED", "LATE", "GRADED"

    @Column(precision = 6, scale = 2)
    private BigDecimal marksAwarded;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    private LocalDateTime gradedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "graded_by_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "passwordHash"})
    private User gradedBy;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Builder.Default
    private int submissionVersion = 1;
}
