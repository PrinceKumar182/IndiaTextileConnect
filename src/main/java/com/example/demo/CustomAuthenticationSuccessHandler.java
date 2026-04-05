package com.example.demo;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final LoginLogRepository loginLogRepository;
    private final UserRepository userRepository;

    public CustomAuthenticationSuccessHandler(LoginLogRepository loginLogRepository, UserRepository userRepository) {
        this.loginLogRepository = loginLogRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String username = authentication.getName();
        try {
            LoginLog log = new LoginLog(username, LocalDateTime.now());
            loginLogRepository.save(log);

            // Update user's last login
            userRepository.findByUsername(username).ifPresent(user -> {
                user.setLastLogin(LocalDateTime.now());
                userRepository.save(user);
            });
        } catch (Exception e) {
            // Log the error but don't fail the login
            System.err.println("Failed to log login event: " + e.getMessage());
        }

        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            response.sendRedirect("/admin");
        } else {
            response.sendRedirect("/");
        }
    }
}