package ru.lms_project.groupservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import ru.lms_project.groupservice.dto.GroupCreateRequest;
import ru.lms_project.groupservice.dto.GroupCreateResponse;
import ru.lms_project.groupservice.dto.GroupDto;
import ru.lms_project.groupservice.model.Group;
import ru.lms_project.groupservice.repository.GroupRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @InjectMocks
    private GroupService groupService;

    private Group testGroup;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testGroup = new Group();
        testGroup.setId(testId);
        testGroup.setName("Test Group");
        testGroup.setDescription("Test Description");
        testGroup.setActive(true);
    }

    @Test
    void getAllGroups_ShouldReturnListOfGroups() {
        // Arrange
        when(groupRepository.findAll()).thenReturn(Arrays.asList(testGroup));

        // Act
        List<GroupDto> result = groupService.getAllGroups();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Group", result.get(0).getName());
        verify(groupRepository, times(1)).findAll();
    }

    @Test
    void getById_WhenGroupExists_ShouldReturnGroup() {
        // Arrange
        when(groupRepository.findById(testId)).thenReturn(Optional.of(testGroup));

        // Act
        GroupDto result = groupService.getById(testId);

        // Assert
        assertNotNull(result);
        assertEquals(testId, result.getId());
        assertEquals("Test Group", result.getName());
        verify(groupRepository, times(1)).findById(testId);
    }

    @Test
    void getById_WhenGroupNotExists_ShouldThrowException() {
        // Arrange
        when(groupRepository.findById(testId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> groupService.getById(testId));
        verify(groupRepository, times(1)).findById(testId);
    }

    @Test
    void create_WithValidRequest_ShouldCreateGroup() {
        // Arrange
        GroupCreateRequest request = new GroupCreateRequest();
        request.setName("New Group");
        request.setDescription("New Description");
        request.setActive(true);

        when(groupRepository.existsByName("New Group")).thenReturn(false);
        when(groupRepository.save(any(Group.class))).thenReturn(testGroup);

        // Act
        GroupCreateResponse result = groupService.create(request);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getId());
        verify(groupRepository, times(1)).existsByName("New Group");
        verify(groupRepository, times(1)).save(any(Group.class));
    }

    @Test
    void create_WithDuplicateName_ShouldThrowException() {
        // Arrange
        GroupCreateRequest request = new GroupCreateRequest();
        request.setName("Existing Group");

        when(groupRepository.existsByName("Existing Group")).thenReturn(true);

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> groupService.create(request));
        verify(groupRepository, times(1)).existsByName("Existing Group");
        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    void deleteById_WhenGroupExists_ShouldDeleteGroup() {
        // Arrange
        when(groupRepository.existsById(testId)).thenReturn(true);
        doNothing().when(groupRepository).deleteById(testId);

        // Act
        groupService.deleteById(testId);

        // Assert
        verify(groupRepository, times(1)).existsById(testId);
        verify(groupRepository, times(1)).deleteById(testId);
    }

    @Test
    void deleteById_WhenGroupNotExists_ShouldThrowException() {
        // Arrange
        when(groupRepository.existsById(testId)).thenReturn(false);

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> groupService.deleteById(testId));
        verify(groupRepository, times(1)).existsById(testId);
        verify(groupRepository, never()).deleteById(testId);
    }
}
