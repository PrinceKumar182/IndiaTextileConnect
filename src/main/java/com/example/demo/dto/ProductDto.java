package com.example.demo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public class ProductDto {

    @NotBlank(message = "Product name is required")
    private String name;

    private String description;

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
    private double price;

    private MultipartFile imageFile;
    private List<MultipartFile> extraImageFiles;
    private List<MultipartFile> videoFiles;

    private boolean isMostlyBought;
    private boolean isNewlyAdded;

    @NotBlank(message = "Hub (Place) is required")
    private String placeId;

    @NotBlank(message = "Category is required")
    private String categoryId;

    @Min(value = 1, message = "Min quantity must be at least 1")
    private int minQuantity = 1;

    @Min(value = 1, message = "Available sets must be at least 1")
    private int setsAvailable;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public MultipartFile getImageFile() { return imageFile; }
    public void setImageFile(MultipartFile imageFile) { this.imageFile = imageFile; }
    public List<MultipartFile> getExtraImageFiles() { return extraImageFiles; }
    public void setExtraImageFiles(List<MultipartFile> extraImageFiles) { this.extraImageFiles = extraImageFiles; }
    public List<MultipartFile> getVideoFiles() { return videoFiles; }
    public void setVideoFiles(List<MultipartFile> videoFiles) { this.videoFiles = videoFiles; }
    public boolean isMostlyBought() { return isMostlyBought; }
    public void setMostlyBought(boolean mostlyBought) { isMostlyBought = mostlyBought; }
    public boolean isNewlyAdded() { return isNewlyAdded; }
    public void setNewlyAdded(boolean newlyAdded) { isNewlyAdded = newlyAdded; }
    public String getPlaceId() { return placeId; }
    public void setPlaceId(String placeId) { this.placeId = placeId; }
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public int getMinQuantity() { return minQuantity; }
    public void setMinQuantity(int minQuantity) { this.minQuantity = minQuantity; }
    public int getSetsAvailable() { return setsAvailable; }
    public void setSetsAvailable(int setsAvailable) { this.setsAvailable = setsAvailable; }
}
