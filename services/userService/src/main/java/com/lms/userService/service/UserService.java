package com.lms.userService.service;

import com.lms.userService.model.Role;
import com.lms.userService.model.User;
import com.lms.userService.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor

@Service
public class UserService {
    private final UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    public User getUserById(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }

    //TODO CREATION USERS

	public User updateUser(UUID id, User user) {
		User existing = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
		existing.setFirstName(user.getFirstName());
		existing.setLastName(user.getLastName());
		existing.setEmail(user.getEmail());
		existing.setPassword(user.getPassword());
		existing.setRole(user.getRole());
		return userRepository.save(existing);
	}

	public void deleteUser(UUID id) {
		userRepository.deleteById(id);
	}

	public User updateRole(UUID id, Role role) {
		User existing = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
		existing.setRole(role);
		return userRepository.save(existing);
	}
}
