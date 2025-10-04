package ru.lms_project.authservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(name = "LoginRequest", description = "Authorization request by email and password")
public class LoginRequest {
    @NotBlank(message = "Email is invalid")
    @Email(message = "Email is invalid")
    @Schema(description = "User email", example = "pochta123@mail.ru")
    private String email;

    @NotBlank(message = "Password can't be blank")
    @Schema(description = "User password", example = "yakrutoi1234")
    private String password;
}
