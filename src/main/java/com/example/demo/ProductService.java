package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private MongoTemplate mongoTemplate;

    public Page<Product> filterProducts(String placeId, String categoryId, String searchQuery, Pageable pageable) {
        Query query = new Query();
        
        // Base Requirement: Active items only
        query.addCriteria(Criteria.where("setsAvailable").gt(0));

        // Optional Filters
        if (placeId != null && !placeId.isBlank()) {
            query.addCriteria(Criteria.where("placeId").is(placeId));
        }

        if (categoryId != null && !categoryId.isBlank()) {
            query.addCriteria(Criteria.where("categoryId").is(categoryId));
        }

        if (searchQuery != null && !searchQuery.isBlank()) {
            query.addCriteria(Criteria.where("name").regex(searchQuery, "i"));
        }

        // Apply Pagination & Sorting
        Query countQuery = Query.of(query); // Clone to count total before applying limits
        
        query.with(pageable);

        List<Product> products = mongoTemplate.find(query, Product.class);
        
        return PageableExecutionUtils.getPage(
                products, 
                pageable, 
                () -> mongoTemplate.count(countQuery, Product.class)
        );
    }
}
