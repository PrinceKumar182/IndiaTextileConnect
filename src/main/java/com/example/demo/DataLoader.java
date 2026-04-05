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

            // Add initial places
            if (placeRepository != null && placeRepository.count() == 0) {
                placeRepository.save(new Place("Kolkata"));
                placeRepository.save(new Place("Delhi"));
                placeRepository.save(new Place("Surat"));
                placeRepository.save(new Place("Ahmedabad"));
                placeRepository.save(new Place("Varanasi"));
                System.out.println("✅ Initial places added");
            }

            // Add admin user
            if (userRepository != null && userRepository.findByUsername("9931435323").isEmpty()) {
                userRepository.save(new User("9931435323", passwordEncoder.encode("Papa@8055"), "ADMIN", "9931435323"));
            }

            // Add sample products
            if (productRepository != null && placeRepository != null && productRepository.count() == 0) {
                Place kolkata = placeRepository.findAll().stream().filter(p -> p.getName().equals("Kolkata")).findFirst().orElse(null);
                if (kolkata != null) {
                productRepository.save(new Product("Cotton Fabric", "High quality cotton", 100.0, "https://via.placeholder.com/300x200?text=Cotton+Fabric", true, true, kolkata.getId(), 10, 50));
                productRepository.save(new Product("Silk Saree", "Beautiful silk saree", 500.0, "https://via.placeholder.com/300x200?text=Silk+Saree", false, true, kolkata.getId(), 1, 20));
                    System.out.println("✅ Sample products added");
                }
            }

        } catch (Exception e) {
            System.err.println("❌ MongoDB connection failed: " + e.getMessage());
            e.printStackTrace();
            // Don't throw exception to allow app to start
        }
    }
}