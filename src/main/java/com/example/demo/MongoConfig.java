package com.example.demo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongoConfig {

    public MongoConfig() {
        System.out.println("🔧 MongoConfig constructor called");
    }

    @Bean
    public MongoClient mongoClient() {
        String uri = "mongodb+srv://prince21182_db_user:u1YTb1sqeBJQKSb6@cluster0.uz6uptp.mongodb.net/textile";
        System.out.println("🔧 Creating MongoClient with URI: " + uri);
        MongoClient client = MongoClients.create(uri);
        System.out.println("🔧 MongoClient created successfully");
        return client;
    }

    @Bean
    public MongoTemplate mongoTemplate(MongoClient mongoClient) {
        System.out.println("🔧 Creating MongoTemplate with database: textile");
        MongoTemplate template = new MongoTemplate(mongoClient, "textile");
        System.out.println("🔧 MongoTemplate created successfully");
        return template;
    }
}