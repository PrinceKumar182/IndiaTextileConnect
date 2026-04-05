package com.example.demo;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationEventsListener {

    private final LoginAttemptService loginAttemptService;

    public AuthenticationEventsListener(LoginAttemptService loginAttemptService) {
        this.loginAttemptService = loginAttemptService;
    }

    @EventListener
    public void onAuthenticationFailure(AuthenticationFailureBadCredentialsEvent e) {
        String username = e.getAuthentication().getName();
        loginAttemptService.loginFailed(username);
    }

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent e) {
        String username = e.getAuthentication().getName();
        loginAttemptService.loginSucceeded(username);
    }
}
