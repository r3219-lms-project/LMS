package ru.lms_project.userservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.lms_project.userservice.dto.UserCreateRequest;
import ru.lms_project.userservice.dto.UserCreateResponse;
import ru.lms_project.userservice.dto.UserDto;
import ru.lms_project.userservice.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable UUID id) {
        return userService.getById(id);
    }

    @GetMapping("/by-email")
    public ResponseEntity<?> getByEmail(@RequestParam("email") String email) {
        try {
            UserDto user = userService.getByEmail(email);
            return ResponseEntity.ok(user);
        } catch (ResponseStatusException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                Map<String, Object> body = new HashMap<>();
                body.put("error", "user_not_found");
                body.put("message", "User with this email does not exist");
                return ResponseEntity.status(HttpStatus.ACCEPTED).body(body);
            }
            throw e;
        }
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserCreateResponse createUser(@RequestBody UserCreateRequest req) {
        return userService.create(req);
    }
}
