package ru.lms_project.authservice.controller;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.lms_project.authservice.dto.LoginRequest;
import ru.lms_project.authservice.dto.LoginResponse;
import ru.lms_project.authservice.dto.RefreshRequest;
import ru.lms_project.authservice.exceptions.AuthExceptionHandler;
import ru.lms_project.authservice.service.AuthService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {
    @Autowired
    MockMvc mvc;

    @MockitoBean
    AuthService authService;

    @Test
    void login_returnsTokens() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(new LoginResponse("access", "refresh"));

        mvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"user@example.com\",\"password\":\"pass\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access"))
                .andExpect(jsonPath("$.refreshToken").value("refresh"));
    }

    @Test
    void refresh_returnsNewTokens() throws Exception {
        when(authService.refreshToken(any(RefreshRequest.class)))
                .thenReturn(new LoginResponse("newAccess", "newRefresh"));

        mvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oldRefreshToken\":\"xxx.yyy.zzz\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("newAccess"))
                .andExpect(jsonPath("$.refreshToken").value("newRefresh"));
    }

    @Test
    void refresh_invalidToken_returns401() throws Exception {
        when(authService.refreshToken(any(RefreshRequest.class)))
                .thenThrow(new ru.lms_project.authservice.exceptions.TokenValidationException("invalid_token", "bad jwt"));

        mvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oldRefreshToken\":\"broken\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("invalid_token"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

}
