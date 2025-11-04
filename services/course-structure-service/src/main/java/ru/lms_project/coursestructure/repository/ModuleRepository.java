package ru.lms_project.coursestructure.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import ru.lms_project.coursestructure.model.Module;

import java.util.List;

@Repository
public interface ModuleRepository extends MongoRepository<Module, String> {
    List<Module> findByCourseIdOrderByOrderIndexAsc(String courseId);
    void deleteByCourseId(String courseId);
    long countByCourseId(String courseId);
}
