package com.example.demo;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);
    Optional<User> findByPhone(String phone);
    Optional<User> findFirstByPhone(String phone);     // safe when duplicates exist
    Optional<User> findFirstByUsername(String username);
    List<User> findAllByPhone(String phone);           // for cleanup
}