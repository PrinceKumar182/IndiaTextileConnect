package com.example.demo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "login_logs")
public class LoginLog {
    @Id
    private String id;
    private String username;
    private LocalDateTime loginTime;

    public LoginLog() {}

    public LoginLog(String username, LocalDateTime loginTime) {
        this.username = username;
        this.loginTime = loginTime;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public LocalDateTime getLoginTime() { return loginTime; }
    public void setLoginTime(LocalDateTime loginTime) { this.loginTime = loginTime; }
}