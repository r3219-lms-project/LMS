package ru.lms_project.coursestructure.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.lms_project.coursestructure.model.LessonType;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LessonDto {
    private String id;
    private String moduleId;
    private String title;
    private String content;
    private LessonType type;
    private Integer duration;
    private Integer orderIndex;
    private String videoUrl;
    private LocalDateTime createdAt;
}
