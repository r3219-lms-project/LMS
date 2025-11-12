package com.lms.progressService.repository;

import com.lms.progressService.model.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LessonProgressRepository extends JpaRepository<LessonProgress, UUID> {

    Optional<LessonProgress> findByUserIdAndLessonId(UUID userId, UUID lessonId);

    List<LessonProgress> findByUserIdAndCourseId(UUID userId, UUID courseId);

    List<LessonProgress> findByUserId(UUID userId);

    @Query("SELECT COUNT(lp) FROM LessonProgress lp WHERE lp.userId = :userId AND lp.courseId = :courseId AND lp.completed = true")
    long countCompletedLessonsByCourseAndUser(@Param("userId") UUID userId, @Param("courseId") UUID courseId);

    @Query("SELECT COUNT(DISTINCT lp.lessonId) FROM LessonProgress lp WHERE lp.courseId = :courseId")
    long countTotalLessonsByCourse(@Param("courseId") UUID courseId);
}
