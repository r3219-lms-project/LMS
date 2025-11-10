package ru.lms_project.coursestructure.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.lms_project.coursestructure.dto.LessonCreateRequest;
import ru.lms_project.coursestructure.dto.LessonDto;
import ru.lms_project.coursestructure.dto.LessonUpdateRequest;
import ru.lms_project.coursestructure.model.Lesson;
import ru.lms_project.coursestructure.repository.LessonRepository;
import ru.lms_project.coursestructure.repository.ModuleRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final ModuleRepository moduleRepository;

    public LessonDto createLesson(String moduleId, LessonCreateRequest request) {
        if (!moduleRepository.existsById(moduleId)) {
            throw new RuntimeException("Module not found with id: " + moduleId);
        }

        Lesson lesson = new Lesson();
        lesson.setModuleId(moduleId);
        lesson.setTitle(request.getTitle());
        lesson.setContent(request.getContent());
        lesson.setType(request.getType());
        lesson.setDuration(request.getDuration());
        lesson.setOrderIndex(request.getOrderIndex());
        lesson.setVideoUrl(request.getVideoUrl());

        Lesson saved = lessonRepository.save(lesson);
        return toDto(saved);
    }

    public List<LessonDto> getLessonsByModuleId(String moduleId) {
        return lessonRepository.findByModuleIdOrderByOrderIndexAsc(moduleId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public LessonDto getLessonById(String id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lesson not found with id: " + id));
        return toDto(lesson);
    }

    public LessonDto updateLesson(String id, LessonUpdateRequest request) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lesson not found with id: " + id));

        if (request.getTitle() != null) {
            lesson.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            lesson.setContent(request.getContent());
        }
        if (request.getType() != null) {
            lesson.setType(request.getType());
        }
        if (request.getDuration() != null) {
            lesson.setDuration(request.getDuration());
        }
        if (request.getOrderIndex() != null) {
            lesson.setOrderIndex(request.getOrderIndex());
        }
        if (request.getVideoUrl() != null) {
            lesson.setVideoUrl(request.getVideoUrl());
        }

        Lesson updated = lessonRepository.save(lesson);
        return toDto(updated);
    }

    public void deleteLesson(String id) {
        if (!lessonRepository.existsById(id)) {
            throw new RuntimeException("Lesson not found with id: " + id);
        }
        lessonRepository.deleteById(id);
    }

    private LessonDto toDto(Lesson lesson) {
        LessonDto dto = new LessonDto();
        dto.setId(lesson.getId());
        dto.setModuleId(lesson.getModuleId());
        dto.setTitle(lesson.getTitle());
        dto.setContent(lesson.getContent());
        dto.setType(lesson.getType());
        dto.setDuration(lesson.getDuration());
        dto.setOrderIndex(lesson.getOrderIndex());
        dto.setVideoUrl(lesson.getVideoUrl());
        dto.setCreatedAt(lesson.getCreatedAt());
        return dto;
    }
}
