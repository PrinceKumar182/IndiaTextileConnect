package com.example.demo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public class CarouselMediaDto {

    @NotBlank(message = "Type must be either IMAGE or VIDEO")
    private String type;

    private String title;
    private String subtitle;

    private int orderIndex;
    
    @NotNull(message = "Media file is required")
    private MultipartFile mediaFile;

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
    public MultipartFile getMediaFile() { return mediaFile; }
    public void setMediaFile(MultipartFile mediaFile) { this.mediaFile = mediaFile; }
}
