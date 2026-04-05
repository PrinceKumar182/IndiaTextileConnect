package com.example.demo;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.service-account.path}")
    private String serviceAccountPath;

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                InputStream serviceAccount;
                        
                org.springframework.core.io.ClassPathResource classPathResource = new org.springframework.core.io.ClassPathResource(serviceAccountPath);
                if (classPathResource.exists()) {
                    serviceAccount = classPathResource.getInputStream();
                } else {
                            
                    serviceAccount = new java.io.FileInputStream(new java.io.File("src/main/resources", serviceAccountPath));
                }

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();
                FirebaseApp.initializeApp(options);
                System.out.println("✅ Firebase Admin SDK initialized successfully");
            }
        } catch (Exception e) {
            System.err.println("❌ Firebase Admin SDK initialization failed: " + e.getMessage());
            // App continues — Firebase will be unavailable but won't crash startup
        }
    }
}
