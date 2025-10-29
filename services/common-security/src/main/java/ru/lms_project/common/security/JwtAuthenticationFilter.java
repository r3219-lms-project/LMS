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
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String token = null;

        if (authHeader == null) {
            throw new JwtTokenException("Authorization header is empty");
        }

        if (authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            ParsedToken parsedToken = jwtTokenProvider.parseAccessToken(token);
            UserPrincipal userPrincipal = new UserPrincipal(
                    parsedToken.getUserId(),
                    parsedToken.getRoles()
            );

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (JwtTokenException e) {
            throw new JwtTokenException("invalid_token");
        }
    }
}
