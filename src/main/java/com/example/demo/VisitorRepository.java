package com.example.demo;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface VisitorRepository extends MongoRepository<Visitor, String> {
}