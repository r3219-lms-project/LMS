package com.example.lms.groups.controller;

import com.example.lms.groups.dto.GroupDto;
import com.example.lms.groups.entity.Group;
import com.example.lms.groups.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

  private final GroupService groupService;

  @Operation(summary = "Создать группу")
  @ApiResponse(responseCode = "200", description = "Группа создана")
  @PostMapping
  public ResponseEntity<GroupDto> createGroup(@RequestBody GroupDto dto) {
    Group entity = Group.builder()
        .courseId(dto.getCourseId())
        .startDate(dto.getStartDate())
        .usersId(dto.getUsersId())
        .build();
    Group saved = groupService.createGroup(entity);
    return ResponseEntity.ok(toDto(saved));
  }

  @Operation(summary = "Добавить пользователя в группу")
  @PostMapping("/{groupId}/users/{userId}")
  public ResponseEntity<GroupDto> addUser(@PathVariable UUID groupId, @PathVariable UUID userId) {
    Group updated = groupService.addUserToGroup(groupId, userId);
    return ResponseEntity.ok(toDto(updated));
  }

  @Operation(summary = "Удалить пользователя из группы")
  @DeleteMapping("/{groupId}/users/{userId}")
  public ResponseEntity<GroupDto> removeUser(@PathVariable UUID groupId, @PathVariable UUID userId) {
    Group updated = groupService.removeUserFromGroup(groupId, userId);
    return ResponseEntity.ok(toDto(updated));
  }

  @Operation(summary = "Получить группу по id")
  @GetMapping("/{groupId}")
  public ResponseEntity<GroupDto> getGroup(@PathVariable UUID groupId) {
    return ResponseEntity.ok(toDto(groupService.getGroup(groupId)));
  }

  @Operation(summary = "Список всех групп")
  @GetMapping
  public ResponseEntity<java.util.List<GroupDto>> getAll() {
    return ResponseEntity.ok(groupService.getAllGroups().stream().map(this::toDto).collect(Collectors.toList()));
  }

  private GroupDto toDto(Group g) {
    return GroupDto.builder()
        .id(g.getId())
        .courseId(g.getCourseId())
        .startDate(g.getStartDate())
        .usersId(g.getUsersId())
        .build();
  }
}
