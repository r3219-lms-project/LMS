package com.lms.taskservice.controller;

import com.lms.taskservice.dto.TaskDto;
import com.lms.taskservice.model.Task;
import com.lms.taskservice.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "Создать задачу")
    @ApiResponse(responseCode = "200", description = "Задача создана")
    @PostMapping
    public ResponseEntity<TaskDto> createTask(@RequestBody TaskDto dto) {
        Task entity = Task.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .type(dto.getType())
                .courseId(dto.getCourseId())
                .build();
        Task saved = taskService.createTask(entity);
        return ResponseEntity.ok(toDto(saved));
    }

    @Operation(summary = "Получить все задачи")
    @GetMapping
    public ResponseEntity<List<TaskDto>> getAllTasks() {
        List<Task> tasks = taskService.getAllTasks();
        List<TaskDto> taskDtos = tasks.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(taskDtos);
    }

    @Operation(summary = "Получить задачу по ID")
    @GetMapping("/{id}")
    public ResponseEntity<TaskDto> getTaskById(@PathVariable String id) {
        Task task = taskService.getTaskById(id);
        return ResponseEntity.ok(toDto(task));
    }

    @Operation(summary = "Обновить задачу")
    @PutMapping("/{id}")
    public ResponseEntity<TaskDto> updateTask(@PathVariable String id, @RequestBody TaskDto dto) {
        Task entity = Task.builder()
                .id(id)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .type(dto.getType())
                .courseId(dto.getCourseId())
                .build();
        Task updated = taskService.updateTask(entity);
        return ResponseEntity.ok(toDto(updated));
    }

    @Operation(summary = "Удалить задачу")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable String id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Получить задачи по курсу")
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<TaskDto>> getTasksByCourse(@PathVariable UUID courseId) {
        List<Task> tasks = taskService.getTasksByCourseId(courseId);
        List<TaskDto> taskDtos = tasks.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(taskDtos);
    }

    private TaskDto toDto(Task task) {
        return TaskDto.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .type(task.getType())
                .courseId(task.getCourseId())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}
