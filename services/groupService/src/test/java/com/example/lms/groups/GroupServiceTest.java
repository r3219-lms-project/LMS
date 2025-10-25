package com.example.lms.groups;

import com.example.lms.groups.client.CourseServiceClient;
import com.example.lms.groups.client.UserServiceClient;
import com.example.lms.groups.entity.Group;
import com.example.lms.groups.repository.GroupRepository;
import com.example.lms.groups.service.GroupService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GroupServiceTest {

  private final GroupRepository repository = Mockito.mock(GroupRepository.class);
  private final UserServiceClient userServiceClient = Mockito.mock(UserServiceClient.class);
  private final CourseServiceClient courseServiceClient = Mockito.mock(CourseServiceClient.class);
  private final GroupService service = new GroupService(repository, userServiceClient, courseServiceClient);

  @Test
  void createGroup_success() {
    UUID courseId = UUID.randomUUID();
    Group saved = Group.builder()
        .id(UUID.randomUUID())
        .courseId(courseId)
        .startDate(LocalDate.now())
        .build();

    Mockito.when(courseServiceClient.validateCourse(courseId)).thenReturn(true);
    Mockito.when(repository.save(Mockito.any(Group.class))).thenReturn(saved);

    Group result = service.createGroup(courseId, LocalDate.now());
    assertNotNull(result.getId());
    assertEquals(courseId, result.getCourseId());
  }

  @Test
  void addUser_success() {
    UUID groupId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    Group group = Group.builder()
        .id(groupId)
        .courseId(UUID.randomUUID())
        .startDate(LocalDate.now())
        .build();

    Mockito.when(userServiceClient.validateUser(userId)).thenReturn(true);
    Mockito.when(repository.findById(groupId)).thenReturn(Optional.of(group));
    Mockito.when(repository.save(Mockito.any(Group.class))).thenAnswer(inv -> inv.getArgument(0));

    Group updated = service.addUserToGroup(groupId, userId);
    assertTrue(updated.getUsersId().contains(userId));
  }
}
