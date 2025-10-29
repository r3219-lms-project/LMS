package ru.lms_project.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.lms_project.common.security.exceptions.UnauthorizedException;

import java.util.List;
import java.util.UUID;

public class SecurityUtils {

    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getName() == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            return false;
        }

        return true;
    }

    public static UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!isAuthenticated()) {
            throw new UnauthorizedException("user_not_authorized");
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userPrincipal.getUserId();
    }

    public static List<String> getCurrentUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!isAuthenticated()) {
            throw new UnauthorizedException("user_not_authorized");
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userPrincipal.getRoles();
    }

    public static boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!isAuthenticated()) {
            return false;
        }

        List<String> roles = getCurrentUserRoles();

        return roles.contains("ADMIN");
    }

    public static boolean isCurrentUser(UUID userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!isAuthenticated()) {
            throw new UnauthorizedException("user_not_authorized");
        }

        UUID currentUserId = UUID.fromString(authentication.getName());

        return userId.equals(currentUserId);
    }
}
