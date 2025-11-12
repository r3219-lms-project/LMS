package ru.lms_project.groupservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.lms_project.groupservice.dto.GroupCreateRequest;
import ru.lms_project.groupservice.dto.GroupCreateResponse;
import ru.lms_project.groupservice.dto.GroupDto;
import ru.lms_project.groupservice.dto.GroupUpdateRequest;
import ru.lms_project.groupservice.service.GroupService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;

    @GetMapping
    public List<GroupDto> getAllGroups() {
        return groupService.getAllGroups();
    }

    @GetMapping("/{id}")
    public GroupDto getGroupById(@PathVariable UUID id) {
        return groupService.getById(id);
    }

    @GetMapping("/by-teacher/{teacherId}")
    public List<GroupDto> getGroupsByTeacher(@PathVariable UUID teacherId) {
        return groupService.getByTeacherId(teacherId);
    }

    @GetMapping("/by-course/{courseId}")
    public List<GroupDto> getGroupsByCourse(@PathVariable UUID courseId) {
        return groupService.getByCourseId(courseId);
    }

    @GetMapping("/active")
    public List<GroupDto> getActiveGroups() {
        return groupService.getActiveGroups();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GroupCreateResponse createGroup(@RequestBody GroupCreateRequest req) {
        return groupService.create(req);
    }

    @PutMapping("/{id}")
    public GroupDto updateGroup(@PathVariable UUID id, @RequestBody GroupUpdateRequest req) {
        return groupService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGroup(@PathVariable UUID id) {
        groupService.deleteById(id);
    }
}

