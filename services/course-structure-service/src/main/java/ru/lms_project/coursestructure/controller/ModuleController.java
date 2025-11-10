package ru.lms_project.coursestructure.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.lms_project.common.security.annotation.RequireAdmin;
import ru.lms_project.coursestructure.dto.ModuleCreateRequest;
import ru.lms_project.coursestructure.dto.ModuleDto;
import ru.lms_project.coursestructure.dto.ModuleUpdateRequest;
import ru.lms_project.coursestructure.service.ModuleService;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleService moduleService;

    @PostMapping("/courses/{courseId}/modules")
    @ResponseStatus(HttpStatus.CREATED)
    @RequireAdmin
    public ModuleDto createModule(
            @PathVariable String courseId,
            @Valid @RequestBody ModuleCreateRequest request) {
        return moduleService.createModule(courseId, request);
    }

    @GetMapping("/courses/{courseId}/modules")
    public List<ModuleDto> getModulesByCourse(@PathVariable String courseId) {
        return moduleService.getModulesByCourseId(courseId);
    }

    @GetMapping("/modules/{id}")
    public ModuleDto getModuleById(@PathVariable String id) {
        return moduleService.getModuleById(id);
    }

    @PutMapping("/modules/{id}")
    @RequireAdmin
    public ModuleDto updateModule(
            @PathVariable String id,
            @Valid @RequestBody ModuleUpdateRequest request) {
        return moduleService.updateModule(id, request);
    }

    @DeleteMapping("/modules/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequireAdmin
    public void deleteModule(@PathVariable String id) {
        moduleService.deleteModule(id);
    }
}
