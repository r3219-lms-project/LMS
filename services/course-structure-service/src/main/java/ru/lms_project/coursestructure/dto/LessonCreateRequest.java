package ru.lms_project.coursestructure.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.lms_project.coursestructure.model.LessonType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LessonCreateRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String content;

    @NotNull(message = "Lesson type is required")
    private LessonType type;

    private Integer duration;

    @NotNull(message = "Order index is required")
    private Integer orderIndex;

    private String videoUrl;
}

