package com.example.lms.groups.service;

import com.example.lms.groups.entity.Group;
import com.example.lms.groups.repository.GroupRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroupService {

  private final GroupRepository repository;

  public Group createGroup(Group group) {
    return repository.save(group);
  }

  public Group createGroup(UUID courseId, LocalDate startDate) {
    Group group = Group.builder()
        .courseId(courseId)
        .startDate(startDate)
        .build();
    return repository.save(group);
  }

  public Group addUserToGroup(UUID groupId, UUID userId) {
    Group group = repository.findById(groupId).orElseThrow(() -> new EntityNotFoundException("Group not found"));
    if (group.getUsersId() == null) group.setUsersId(new java.util.ArrayList<>());
    if (!group.getUsersId().contains(userId)) {
      group.getUsersId().add(userId);
    }
    return repository.save(group);
  }

  public Group removeUserFromGroup(UUID groupId, UUID userId) {
    Group group = repository.findById(groupId).orElseThrow(() -> new EntityNotFoundException("Group not found"));
    if (group.getUsersId() != null) {
      group.getUsersId().remove(userId);
    }
    return repository.save(group);
  }

  public List<Group> getAllGroups() {
    return repository.findAll();
  }

  public Group getGroup(UUID id) {
    return repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Group not found"));
  }

  public void deleteGroup(UUID id) {
    repository.deleteById(id);
  }
}
