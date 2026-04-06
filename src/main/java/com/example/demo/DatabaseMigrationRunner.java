package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DatabaseMigrationRunner implements CommandLineRunner {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🚀 Running Database Migrations...");

        // Strategy for Orphaned Products Migration
        String uncategorizedId;
        List<Category> uncatList = categoryRepository.findByNameContainingIgnoreCase("Uncategorized");
        if (uncatList.isEmpty()) {
            Category uncat = new Category("Uncategorized", "Default category for migrated products");
            categoryRepository.save(uncat);
            uncategorizedId = uncat.getId();
        } else {
            uncategorizedId = uncatList.get(0).getId();
        }

        // Migrate products with null or missing categoryId
        Query orphanQuery = new Query();
        orphanQuery.addCriteria(new Criteria().orOperator(
                Criteria.where("categoryId").exists(false),
                Criteria.where("categoryId").is(null)
        ));

        Update update = new Update().set("categoryId", uncategorizedId);
        com.mongodb.client.result.UpdateResult result = mongoTemplate.updateMulti(orphanQuery, update, Product.class);

        System.out.println("✅ Migration completed. Orphaned products migrated: " + result.getModifiedCount());
    }
}
