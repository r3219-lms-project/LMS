package com.lms.courseService;

import com.lms.courseService.dto.UpdateCourseRequest;
import com.lms.courseService.model.Course;
import com.lms.courseService.model.CourseStatus;
import com.lms.courseService.repository.CourseRepository;
import com.lms.courseService.service.CourseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository repo;

    @InjectMocks
    private CourseService service;

    private Course sample;

    @BeforeEach
    void setUp() {
        sample = new Course();
        sample.setId("c1");
        sample.setName("Intro to Spring");
        sample.setDescription("Basics");
        sample.setStudents(List.of("u1", "u2"));
        sample.setDuration(30);
        sample.setStatus(CourseStatus.CREATED);
    }

    @Test
    void create_shouldSaveAndReturnCourse() {
        when(repo.save(sample)).thenReturn(sample);

        Course saved = service.create(sample);

        assertThat(saved).isEqualTo(sample);
        verify(repo).save(sample);
        verifyNoMoreInteractions(repo);
    }

    @Test
    void getAll_shouldReturnListFromRepo() {
        when(repo.findAll()).thenReturn(List.of(sample));

        List<Course> all = service.getAll();

        assertThat(all).containsExactly(sample);
        verify(repo).findAll();
        verifyNoMoreInteractions(repo);
    }

    @Test
    void getById_found_returnsEntity() {
        when(repo.findById("c1")).thenReturn(Optional.of(sample));

        Course c = service.getById("c1");

        assertThat(c).isEqualTo(sample);
        verify(repo).findById("c1");
        verifyNoMoreInteractions(repo);
    }

    @Test
    void getById_notFound_returnsNull() {
        when(repo.findById("missing")).thenReturn(Optional.empty());

        Course c = service.getById("missing");

        assertThat(c).isNull();
        verify(repo).findById("missing");
        verifyNoMoreInteractions(repo);
    }

    @Test
    void delete_exists_deletesAndReturnsTrue() {
        when(repo.existsById("c1")).thenReturn(true);

        boolean result = service.delete("c1");

        assertThat(result).isTrue();
        verify(repo).existsById("c1");
        verify(repo).deleteById("c1");
        verifyNoMoreInteractions(repo);
    }

    @Test
    void delete_notExists_returnsFalse() {
        when(repo.existsById("missing")).thenReturn(false);

        boolean result = service.delete("missing");

        assertThat(result).isFalse();
        verify(repo).existsById("missing");
        verifyNoMoreInteractions(repo);
    }

    @Test
    void changeStatus_found_updatesAndSaves() {
        when(repo.findById("c1")).thenReturn(Optional.of(sample));
        when(repo.save(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));

        Course updated = service.changeStatus("c1", CourseStatus.IN_PROGRESS);

        assertThat(updated).isNotNull();
        assertThat(updated.getStatus()).isEqualTo(CourseStatus.IN_PROGRESS);
        verify(repo).findById("c1");
        verify(repo).save(sample);
        verifyNoMoreInteractions(repo);
    }

    @Test
    void changeStatus_notFound_returnsNull() {
        when(repo.findById("missing")).thenReturn(Optional.empty());

        Course updated = service.changeStatus("missing", CourseStatus.IN_PROGRESS);

        assertThat(updated).isNull();
        verify(repo).findById("missing");
        verifyNoMoreInteractions(repo);
    }

    @Test
    void update_found_appliesAllFieldsAndSaves() {
        UpdateCourseRequest req = new UpdateCourseRequest();
        req.setName("Advanced Spring");
        req.setDescription("Deep dive");
        req.setStudents(List.of("u3"));
        req.setDuration(45);
        req.setStatus(CourseStatus.IN_PROGRESS);

        when(repo.findById("c1")).thenReturn(Optional.of(sample));
        when(repo.save(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));

        Course updated = service.update("c1", req);

        assertThat(updated).isNotNull();
        assertThat(updated.getName()).isEqualTo("Advanced Spring");
        assertThat(updated.getDescription()).isEqualTo("Deep dive");
        assertThat(updated.getStudents()).containsExactly("u3");
        assertThat(updated.getDuration()).isEqualTo(45);
        assertThat(updated.getStatus()).isEqualTo(CourseStatus.IN_PROGRESS);

        verify(repo).findById("c1");
        verify(repo).save(sample);
        verifyNoMoreInteractions(repo);
    }

    @Test
    void update_notFound_returnsNull() {
        when(repo.findById("missing")).thenReturn(Optional.empty());
        UpdateCourseRequest req = new UpdateCourseRequest();

        Course updated = service.update("missing", req);

        assertThat(updated).isNull();
        verify(repo).findById("missing");
        verifyNoMoreInteractions(repo);
    }
}
