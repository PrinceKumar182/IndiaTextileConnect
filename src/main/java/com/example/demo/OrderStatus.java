package com.example.demo;

public enum OrderStatus {
    CREATED,           // Basic initialized state
    PAYMENT_PENDING,   // Stock reserved, waiting on gateway
    PAID,              // Gateway webhook/success received securely
    SHIPPED,           // Admin dispatched
    DELIVERED,         // User received
    CANCELLED          // Expired, failed payload, or manual admin drop
}
