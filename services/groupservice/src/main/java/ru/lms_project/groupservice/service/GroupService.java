package ru.lms_project.groupservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.lms_project.groupservice.dto.GroupCreateRequest;
import ru.lms_project.groupservice.dto.GroupCreateResponse;
import ru.lms_project.groupservice.dto.GroupDto;
import ru.lms_project.groupservice.dto.GroupUpdateRequest;
import ru.lms_project.groupservice.model.Group;
import ru.lms_project.groupservice.repository.GroupRepository;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class GroupService {
    private final GroupRepository groupRepository;

    public List<GroupDto> getAllGroups() {
        return groupRepository.findAll().stream().map(this::toDto).toList();
    }

    public GroupDto getById(UUID id) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "group_not_found"));
        return toDto(group);
    }

    public List<GroupDto> getByTeacherId(UUID teacherId) {
        return groupRepository.findByTeacherId(teacherId).stream()
                .map(this::toDto)
                .toList();
    }

    public List<GroupDto> getByCourseId(UUID courseId) {
        return groupRepository.findByCourseId(courseId).stream()
                .map(this::toDto)
                .toList();
    }

    public List<GroupDto> getActiveGroups() {
        return groupRepository.findByActive(true).stream()
                .map(this::toDto)
                .toList();
    }

    public GroupCreateResponse create(GroupCreateRequest req) {
        String name = Objects.requireNonNull(req.getName(), "name is required").trim();

        if (groupRepository.existsByName(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "group_name_already_exists");
        }

        Group group = new Group();
        group.setName(name);
        group.setDescription(req.getDescription());
        group.setTeacherId(req.getTeacherId());
        group.setCourseId(req.getCourseId());
        group.setActive(req.getActive() != null ? req.getActive() : true);

        Group saved = groupRepository.save(group);
        return new GroupCreateResponse(saved.getId());
    }

    public GroupDto update(UUID id, GroupUpdateRequest req) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "group_not_found"));

        if (req.getName() != null) {
            String newName = req.getName().trim();
            if (!newName.equals(group.getName()) && groupRepository.existsByName(newName)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "group_name_already_exists");
            }
            group.setName(newName);
        }

        if (req.getDescription() != null) {
            group.setDescription(req.getDescription());
        }

        if (req.getTeacherId() != null) {
            group.setTeacherId(req.getTeacherId());
        }

        if (req.getCourseId() != null) {
            group.setCourseId(req.getCourseId());
        }

        if (req.getActive() != null) {
            group.setActive(req.getActive());
        }

        Group updated = groupRepository.save(group);
        return toDto(updated);
    }

    public void deleteById(UUID id) {
        if (!groupRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "group_not_found");
        }
        groupRepository.deleteById(id);
    }

    private GroupDto toDto(Group group) {
        return new GroupDto(
                group.getId(),
                group.getName(),
                group.getDescription(),
                group.getTeacherId(),
                group.getCourseId(),
                group.getCreatedAt(),
                group.getUpdatedAt(),
                group.isActive()
        );
    }
}

