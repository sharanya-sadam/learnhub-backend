package com.learnhub.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lessons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    @JsonIgnore
    private CourseModule module;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String videoUrl; // Public/sample video URL or embed URL

    private String localVideoPath; // Stored path for uploaded video file

    private String duration; // e.g., "10:45"

    @Column(nullable = false)
    @Builder.Default
    private int displayOrder = 1;

    private String resourcePath; // Downloadable material/attachment path

    private String resourceName;

    @Column(nullable = false)
    @Builder.Default
    private boolean published = true;
}
