package ru.lms_project.groupservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.lms_project.groupservice.model.Group;

import java.util.List;
import java.util.UUID;

@Repository
public interface GroupRepository extends JpaRepository<Group, UUID> {
    List<Group> findByTeacherId(UUID teacherId);
    List<Group> findByCourseId(UUID courseId);
    List<Group> findByActive(boolean active);
    boolean existsByName(String name);
}

