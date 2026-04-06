package com.example.demo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "carousel_media")
public class CarouselMedia {
    @Id
    private String id;
    private String mediaUrl;
    private String title;
    private String subtitle;
    private String type; // IMAGE or VIDEO
    private int orderIndex;
    private LocalDateTime createdAt;

    public CarouselMedia() {
        this.createdAt = LocalDateTime.now();
    }

    public CarouselMedia(String mediaUrl, String title, String subtitle, String type, int orderIndex) {
        this.mediaUrl = mediaUrl;
        this.title = title;
        this.subtitle = subtitle;
        this.type = type;
        this.orderIndex = orderIndex;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
