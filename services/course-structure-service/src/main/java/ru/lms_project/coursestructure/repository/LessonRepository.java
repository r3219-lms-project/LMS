package ru.lms_project.coursestructure.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import ru.lms_project.coursestructure.model.Lesson;

import java.util.List;

@Repository
public interface LessonRepository extends MongoRepository<Lesson, String> {
    List<Lesson> findByModuleIdOrderByOrderIndexAsc(String moduleId);
    void deleteByModuleId(String moduleId);
    long countByModuleId(String moduleId);

    @Query(value = "{ 'moduleId': { $in: ?0 } }", count = true)
    long countByModuleIdIn(List<String> moduleIds);

    @Query(value = "{ 'moduleId': { $in: ?0 } }")
    List<Lesson> findByModuleIdIn(List<String> moduleIds);
}
