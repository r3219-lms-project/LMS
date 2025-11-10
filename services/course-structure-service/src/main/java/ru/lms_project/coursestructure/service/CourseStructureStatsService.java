package ru.lms_project.coursestructure.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.lms_project.coursestructure.dto.CourseStructureStatsDto;
import ru.lms_project.coursestructure.model.Lesson;
import ru.lms_project.coursestructure.model.Module;
import ru.lms_project.coursestructure.repository.LessonRepository;
import ru.lms_project.coursestructure.repository.ModuleRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseStructureStatsService {

    private final ModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;

    public CourseStructureStatsDto getCourseStructureStats(String courseId) {
        // Получаем количество модулей для курса
        long totalModules = moduleRepository.countByCourseId(courseId);

        // Получаем все модули курса
        List<Module> modules = moduleRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
        List<String> moduleIds = modules.stream()
                .map(Module::getId)
                .collect(Collectors.toList());

        long totalLessons = 0;
        long totalDuration = 0;

        if (!moduleIds.isEmpty()) {
            // Получаем количество уроков
            totalLessons = lessonRepository.countByModuleIdIn(moduleIds);

            // Получаем все уроки и суммируем их продолжительность
            List<Lesson> lessons = lessonRepository.findByModuleIdIn(moduleIds);
            totalDuration = lessons.stream()
                    .filter(lesson -> lesson.getDuration() != null)
                    .mapToLong(Lesson::getDuration)
                    .sum();
        }

        return new CourseStructureStatsDto(courseId, totalModules, totalLessons, totalDuration);
    }
}

