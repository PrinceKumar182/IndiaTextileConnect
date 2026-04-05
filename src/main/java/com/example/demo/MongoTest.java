package com.example.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

@Component
public class MongoTest implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🧪 Testing direct MongoDB connection...");
        try {
            String uri = "mongodb+srv://prince21182_db_user:u1YTb1sqeBJQKSb6@cluster0.uz6uptp.mongodb.net/textile";
            System.out.println("Connecting to: " + uri);
            MongoClient client = MongoClients.create(uri);
            var database = client.getDatabase("textile");
            System.out.println("✅ Connected to database: " + database.getName());
            client.close();
            System.out.println("✅ Connection test successful!");
        } catch (Exception e) {
            System.err.println("❌ Connection test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}