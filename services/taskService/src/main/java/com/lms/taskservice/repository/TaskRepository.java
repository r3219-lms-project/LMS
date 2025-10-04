package com.lms.taskservice.repository;

import com.lms.taskservice.model.Task;
import com.lms.taskservice.model.TaskType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends MongoRepository<Task, String> {

    List<Task> findByType(TaskType type);

    List<Task> findByCourseId(UUID courseId);

    List<Task> findByTitleContainingIgnoreCase(String title);
}
