package com.example.demo;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface CarouselMediaRepository extends MongoRepository<CarouselMedia, String> {
    List<CarouselMedia> findAllByOrderByOrderIndexAsc();
}
