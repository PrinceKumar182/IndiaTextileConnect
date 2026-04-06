package com.example.demo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import java.time.LocalDateTime;

@Document(collection = "products")
public class Product {
    @Id
    private String id;
    private String name;
    private String description;
    private double price;
    private String imageUrl;
    private java.util.List<String> extraImageUrls = new java.util.ArrayList<>();
    private java.util.List<String> videoUrls = new java.util.ArrayList<>();
    private boolean isMostlyBought;
    private boolean isNewlyAdded;
    
    @Indexed
    private String placeId;
    
    @Indexed
    private String categoryId;
    
    private int minQuantity;
    private int setsAvailable;
    
    @Indexed
    private LocalDateTime createdAt;

    public Product() {}

    public Product(String name, String description, double price, String imageUrl, boolean isMostlyBought, boolean isNewlyAdded, String placeId, String categoryId, int minQuantity, int setsAvailable) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.isMostlyBought = isMostlyBought;
        this.isNewlyAdded = isNewlyAdded;
        this.placeId = placeId;
        this.categoryId = categoryId;
        this.minQuantity = minQuantity;
        this.setsAvailable = setsAvailable;
        // Ensure lists are initialized
        this.extraImageUrls = new java.util.ArrayList<>();
        this.videoUrls = new java.util.ArrayList<>();
        this.createdAt = LocalDateTime.now();
    }

    // getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public java.util.List<String> getExtraImageUrls() {
        if (extraImageUrls == null) extraImageUrls = new java.util.ArrayList<>();
        return extraImageUrls;
    }

    public void setExtraImageUrls(java.util.List<String> extraImageUrls) {
        this.extraImageUrls = extraImageUrls;
    }

    public java.util.List<String> getVideoUrls() {
        if (videoUrls == null) videoUrls = new java.util.ArrayList<>();
        return videoUrls;
    }

    public void setVideoUrls(java.util.List<String> videoUrls) {
        this.videoUrls = videoUrls;
    }

    public boolean isMostlyBought() {
        return isMostlyBought;
    }

    public void setMostlyBought(boolean mostlyBought) {
        isMostlyBought = mostlyBought;
    }

    public boolean isNewlyAdded() {
        return isNewlyAdded;
    }

    public void setNewlyAdded(boolean newlyAdded) {
        isNewlyAdded = newlyAdded;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public int getMinQuantity() {
        return minQuantity;
    }

    public void setMinQuantity(int minQuantity) {
        this.minQuantity = minQuantity;
    }

    public int getSetsAvailable() {
        return setsAvailable;
    }

    public void setSetsAvailable(int setsAvailable) {
        this.setsAvailable = setsAvailable;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}