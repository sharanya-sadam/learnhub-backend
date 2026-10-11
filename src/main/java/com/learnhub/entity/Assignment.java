package com.learnhub.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "assignments", indexes = {
    @Index(name = "idx_assignment_course", columnList = "course_id"),
    @Index(name = "idx_assignment_due_date", columnList = "dueDate")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    @JsonIgnore
    private Course course;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String instructions;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal maxMarks;

    @Column(nullable = false)
    private LocalDateTime dueDate;

    @Column(nullable = false)
    @Builder.Default
    private String allowedSubmissionType = "BOTH"; // "TEXT", "FILE", "BOTH"

    @Column(nullable = false)
    @Builder.Default
    private boolean allowLateSubmissions = true;

    private String attachmentPath; // Reference materials provided by instructor

    private String attachmentName;

    @Column(nullable = false)
    @Builder.Default
    private boolean published = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Submission> submissions = new ArrayList<>();
}
