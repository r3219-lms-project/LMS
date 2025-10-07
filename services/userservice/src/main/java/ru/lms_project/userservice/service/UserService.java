package ru.lms_project.userservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.lms_project.userservice.dto.UserCreateRequest;
import ru.lms_project.userservice.dto.UserCreateResponse;
import ru.lms_project.userservice.dto.UserDto;
import ru.lms_project.userservice.model.Role;
import ru.lms_project.userservice.model.User;
import ru.lms_project.userservice.repository.UserRepository;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserService {
	private final UserRepository userRepository;

	public List<UserDto> getAllUsers() {
		return userRepository.findAll().stream().map(this::toDto).toList();
	}

	public UserDto getById(UUID id) {
		User u = userRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("user_not_found"));
		return toDto(u);
	}

	public UserDto getByEmail(String rawEmail) {
		String email = normalizeEmail(rawEmail);
		User u = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user_not_found"));
		return toDto(u);
	}

	public UserCreateResponse create(UserCreateRequest req) {
		String email = normalizeEmail(req.getEmail());
		if (userRepository.existsByEmail(email)) {
			throw new RuntimeException("email_already_exists");
		}

		User u = new User();
		if (req.getId() != null) u.setId(req.getId());

		u.setEmail(email);
		u.setPasswordHash(Objects.requireNonNull(req.getPasswordHash(), "passwordHash is required"));
		u.setRole(Role.valueOf(Objects.requireNonNull(req.getRole(), "role is required")));
		u.setActive(req.getActive() != null ? req.getActive() : true);
		u.setFirstName(req.getFirstName());
		u.setLastName(req.getLastName());

		User saved = userRepository.save(u);
		return new UserCreateResponse(saved.getId());
	}

	public void deleteById(UUID id) {
		userRepository.deleteById(id);
	}

	private String normalizeEmail(String email) {
		return Objects.requireNonNull(email, "email is required")
				.trim()
				.toLowerCase(Locale.ROOT);
	}

	private UserDto toDto(User u) {
		return new UserDto(
				u.getId(),
				u.getEmail(),
				u.getPasswordHash(),
				u.getRole().name(),
				u.isActive(),
				u.getFirstName(),
				u.getLastName()
		);
	}
}
