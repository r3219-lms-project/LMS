package ru.lms_project.common.security.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ru.lms_project.common.security.UserPrincipal;
import ru.lms_project.common.security.exceptions.UnauthorizedException;

@Aspect
@Component
@Slf4j
public class AdminAccessAspect {

    @Before("@annotation(ru.lms_project.common.security.annotation.RequireAdmin)")
    public void checkAdminAccess() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Unauthorized access attempt - no authentication");
            throw new UnauthorizedException("Authentication required");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserPrincipal)) {
            log.warn("Invalid principal type: {}", principal.getClass().getName());
            throw new UnauthorizedException("Invalid authentication");
        }

        UserPrincipal userPrincipal = (UserPrincipal) principal;

        // Проверяем, есть ли у пользователя роль ADMIN или TEACHER
        boolean hasAdminAccess = userPrincipal.getRoles().stream()
                .anyMatch(role -> "ADMIN".equals(role) || "TEACHER".equals(role));

        if (!hasAdminAccess) {
            log.warn("Access denied for user {} with roles {}", userPrincipal.getUserId(), userPrincipal.getRoles());
            throw new UnauthorizedException("Admin or Teacher access required");
        }

        log.debug("Admin access granted for user {} with roles {}", userPrincipal.getUserId(), userPrincipal.getRoles());
    }
}
