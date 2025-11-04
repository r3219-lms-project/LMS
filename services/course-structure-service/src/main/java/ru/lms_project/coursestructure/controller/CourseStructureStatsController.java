package ru.lms_project.coursestructure.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.lms_project.common.security.annotation.RequireAuthenticated;
import ru.lms_project.coursestructure.dto.CourseStructureStatsDto;
import ru.lms_project.coursestructure.service.CourseStructureStatsService;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
@Tag(name = "Course Structure Statistics", description = "API для получения статистики по структуре курса")
@SecurityRequirement(name = "bearerAuth")
public class CourseStructureStatsController {

    private final CourseStructureStatsService courseStructureStatsService;

    @GetMapping("/{courseId}/structure-stats")
    @RequireAuthenticated
    @Operation(
            summary = "Получить статистику по структуре курса",
            description = "Возвращает общее количество модулей, уроков и суммарную продолжительность для указанного курса"
    )
    @ApiResponse(responseCode = "200", description = "Статистика успешно получена")
    @ApiResponse(responseCode = "401", description = "Не аутентифицирован")
    @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    public ResponseEntity<CourseStructureStatsDto> getCourseStructureStats(
            @Parameter(description = "ID курса", required = true)
            @PathVariable String courseId
    ) {
        CourseStructureStatsDto stats = courseStructureStatsService.getCourseStructureStats(courseId);
        return ResponseEntity.ok(stats);
    }
}
