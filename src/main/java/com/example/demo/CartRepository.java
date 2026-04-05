package com.example.demo;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface CartRepository extends MongoRepository<Cart, String> {
    List<Cart> findByUserId(String userId);
    void deleteByUserId(String userId);
    Optional<Cart> findByUserIdAndProductId(String userId, String productId);
}