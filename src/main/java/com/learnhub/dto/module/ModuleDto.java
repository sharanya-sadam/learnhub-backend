package com.learnhub.dto.module;

import com.learnhub.dto.lesson.LessonDto;
import com.learnhub.entity.CourseModule;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModuleDto {
    private Long id;
    private Long courseId;
    private String title;
    private String description;
    private int displayOrder;
    @Builder.Default
    private List<LessonDto> lessons = new ArrayList<>();

    public static ModuleDto fromEntity(CourseModule module, Set<Long> completedLessonIds) {
        List<LessonDto> lessonDtos = new ArrayList<>();
        if (module.getLessons() != null) {
            for (var lesson : module.getLessons()) {
                boolean completed = completedLessonIds != null && completedLessonIds.contains(lesson.getId());
                lessonDtos.add(LessonDto.fromEntity(lesson, completed));
            }
        }
        return ModuleDto.builder()
                .id(module.getId())
                .courseId(module.getCourse() != null ? module.getCourse().getId() : null)
                .title(module.getTitle())
                .description(module.getDescription())
                .displayOrder(module.getDisplayOrder())
                .lessons(lessonDtos)
                .build();
    }
}
