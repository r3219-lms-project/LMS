package ru.lms_project.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.lms_project.common.security.exceptions.JwtTokenException;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            ParsedToken parsedToken = jwtTokenProvider.parseAccessToken(token);

            UserPrincipal userPrincipal = new UserPrincipal(
                    parsedToken.getUserId(),
                    parsedToken.getRoles()
            );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userPrincipal,
                            null,
                            userPrincipal.getAuthorities()
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            // УЛУЧШЕНО: более детальное логирование ошибок валидации токена
            logger.error("JWT validation failed for request [" + request.getMethod() + " " + request.getRequestURI() + "]: " + e.getMessage(), e);
            // Важно: не устанавливаем аутентификацию, запрос продолжится как неаутентифицированный
            // SecurityConfig решит, что делать дальше
        }

        filterChain.doFilter(request, response);
    }
}