package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired(required = false)
    private PlaceRepository placeRepository;

    @Autowired(required = false)
    private ProductRepository productRepository;

    @Autowired(required = false)
    private UserRepository userRepository;

    @Autowired(required = false)
    private VisitorRepository visitorRepository;

    @Autowired(required = false)
    private MongoTemplate mongoTemplate;

    @Autowired
    private Environment environment;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🔍 Checking MongoDB configuration...");
        System.out.println("MongoDB URI from Environment: " + environment.getProperty("spring.data.mongodb.uri"));
        
        if (mongoTemplate == null) {
            System.out.println("❌ MongoDB not configured, skipping data loading");
            return;
        }
        
        try {
            // Test MongoDB connection
            mongoTemplate.getDb().getName();
            System.out.println("✅ MongoDB connected successfully to database: " + mongoTemplate.getDb().getName());

            // Initial places removed to prevent ghost-respawning when user clears their DB.

            // Add admin user
            if (userRepository != null && userRepository.findByUsername("9931435323").isEmpty()) {
                userRepository.save(new User("9931435323", passwordEncoder.encode("Papa@8055"), "ADMIN", "9931435323"));
            }

            // Sample products removed to allow for clean production deployments without dummy rows.

        } catch (Exception e) {
            System.err.println("❌ MongoDB connection failed: " + e.getMessage());
            e.printStackTrace();
            // Don't throw exception to allow app to start
        }
    }
}