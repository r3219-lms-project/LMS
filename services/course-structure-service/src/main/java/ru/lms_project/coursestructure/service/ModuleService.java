package ru.lms_project.coursestructure.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.lms_project.coursestructure.dto.ModuleCreateRequest;
import ru.lms_project.coursestructure.dto.ModuleDto;
import ru.lms_project.coursestructure.dto.ModuleUpdateRequest;
import ru.lms_project.coursestructure.exception.ResourceNotFoundException;
import ru.lms_project.coursestructure.model.Module;
import ru.lms_project.coursestructure.repository.LessonRepository;
import ru.lms_project.coursestructure.repository.ModuleRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ModuleService {

    private final ModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;

    public ModuleDto createModule(String courseId, ModuleCreateRequest request) {
        Module module = new Module();
        module.setCourseId(courseId);
        module.setTitle(request.getTitle());
        module.setDescription(request.getDescription());
        module.setOrderIndex(request.getOrderIndex());

        Module saved = moduleRepository.save(module);
        return toDto(saved);
    }

    public List<ModuleDto> getModulesByCourseId(String courseId) {
        return moduleRepository.findByCourseIdOrderByOrderIndexAsc(courseId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public ModuleDto getModuleById(String id) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found with id: " + id));
        return toDto(module);
    }

    public ModuleDto updateModule(String id, ModuleUpdateRequest request) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found with id: " + id));

        if (request.getTitle() != null) {
            module.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            module.setDescription(request.getDescription());
        }
        if (request.getOrderIndex() != null) {
            module.setOrderIndex(request.getOrderIndex());
        }

        Module updated = moduleRepository.save(module);
        return toDto(updated);
    }

    public void deleteModule(String id) {
        if (!moduleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Module not found with id: " + id);
        }
        lessonRepository.findByModuleIdOrderByOrderIndexAsc(id).forEach(lesson ->
            lessonRepository.deleteById(lesson.getId())
        );
        moduleRepository.deleteById(id);
    }

    private ModuleDto toDto(Module module) {
        ModuleDto dto = new ModuleDto();
        dto.setId(module.getId());
        dto.setCourseId(module.getCourseId());
        dto.setTitle(module.getTitle());
        dto.setDescription(module.getDescription());
        dto.setOrderIndex(module.getOrderIndex());
        dto.setCreatedAt(module.getCreatedAt());

        long lessonsCount = lessonRepository.findByModuleIdOrderByOrderIndexAsc(module.getId()).size();
        dto.setLessonsCount(lessonsCount);

        return dto;
    }
}
