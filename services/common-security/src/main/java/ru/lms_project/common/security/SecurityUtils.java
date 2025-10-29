package ru.lms_project.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.lms_project.common.security.exceptions.UnauthorizedException;

import java.util.List;
import java.util.UUID;

public class SecurityUtils {

    private static boolean isAuthenticated(Authentication authentication) {
        if (authentication.getName() == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            return false;
        }

        return true;
    }

    public static String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!isAuthenticated(authentication)) {
            throw new UnauthorizedException("user_not_authorized");
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userPrincipal.getUsername();
    }

    public static List<GrantedAuthority> getCurrentUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!isAuthenticated(authentication)) {
            throw new UnauthorizedException("user_not_authorized");
        }

        List<GrantedAuthority> authorities = (List<GrantedAuthority>) authentication.getAuthorities();
        return authorities;
    }

    public static boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!isAuthenticated(authentication)) {
            throw new UnauthorizedException("user_not_authorized");
        }

        List<GrantedAuthority> authorities = (List<GrantedAuthority>) authentication.getAuthorities();

        for (GrantedAuthority grantedAuthority : authorities) {
            if (grantedAuthority.getAuthority().contains("ADMIN")) {
                return true;
            }
        }
        return false;
    }

    public static boolean isCurrentUser(UUID userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!isAuthenticated(authentication)) {
            throw new UnauthorizedException("user_not_authorized");
        }

        UUID currentUserId = UUID.fromString(authentication.getName());

        return userId.equals(currentUserId);
    }
}
