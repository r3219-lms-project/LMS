package ru.lms_project.authservice.service;

import org.springframework.stereotype.Service;
import ru.lms_project.authservice.dto.LoginRequest;
import ru.lms_project.authservice.dto.LoginResponse;
import ru.lms_project.authservice.dto.LogoutAllRequest;
import ru.lms_project.authservice.dto.RefreshRequest;

@Service
public interface AuthService {
    LoginResponse login(LoginRequest request);
    LoginResponse refreshToken(RefreshRequest refreshToken);
    void logout(RefreshRequest request);
    void logoutAll(LogoutAllRequest request);
}
