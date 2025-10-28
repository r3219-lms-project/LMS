package ru.lms_project.authservice.service;

import org.springframework.stereotype.Service;
import ru.lms_project.authservice.dto.*;

@Service
public interface AuthService {
    LoginResponse register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
    LoginResponse refreshToken(RefreshRequest refreshToken);
    void logout(RefreshRequest request);
    void logoutAll(LogoutAllRequest request);
}
