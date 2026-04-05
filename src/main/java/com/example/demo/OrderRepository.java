package com.example.demo;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findByUserId(String userId);
    List<Order> findByUserIdOrderByCreatedAtDesc(String userId);
    Order findByRazorpayOrderId(String razorpayOrderId);
    Order findByIdempotencyToken(String idempotencyToken);
}
