package com.example.demo;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ProductRepository extends MongoRepository<Product, String> {
    List<Product> findByPlaceId(String placeId);
    void deleteByPlaceId(String placeId);
    List<Product> findBySetsAvailableGreaterThan(int setsAvailable);
    List<Product> findByIsMostlyBoughtTrueAndSetsAvailableGreaterThan(int setsAvailable);
    List<Product> findByIsNewlyAddedTrueAndSetsAvailableGreaterThan(int setsAvailable);
    List<Product> findByPlaceIdAndSetsAvailableGreaterThan(String placeId, int setsAvailable);
    List<Product> findByNameContainingIgnoreCaseAndSetsAvailableGreaterThan(String name, int setsAvailable);
    List<Product> findBySetsAvailableLessThanEqual(int setsAvailable);
}