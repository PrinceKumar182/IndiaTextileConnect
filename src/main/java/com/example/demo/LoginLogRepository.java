package com.example.demo;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface LoginLogRepository extends MongoRepository<LoginLog, String> {
    List<LoginLog> findAllByOrderByLoginTimeDesc();
}