package com.example.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.upload.dir:src/main/resources/static/uploads}")
    private String uploadDir;

    public String saveFile(MultipartFile file, List<String> allowedExtensions) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty or null");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IllegalArgumentException("Invalid file format");
        }

        String ext = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
        if (allowedExtensions != null && !allowedExtensions.isEmpty() && !allowedExtensions.contains(ext)) {
            throw new IllegalArgumentException("File type not allowed. Supported types: " + allowedExtensions);
        }

        String uniqueName = UUID.randomUUID() + "." + ext;
        Path uploadPath = Paths.get(uploadDir);
        
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        Files.copy(file.getInputStream(), uploadPath.resolve(uniqueName));
        return "/uploads/" + uniqueName;
    }

    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank() || !fileUrl.startsWith("/uploads/")) {
            return;
        }
        
        String filename = fileUrl.substring("/uploads/".length());
        Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
        
        // Anti-directory traversal check
        if (!filePath.startsWith(Paths.get(uploadDir).toAbsolutePath().normalize())) {
            return;
        }

        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("Failed to delete file: " + filePath + " - " + e.getMessage());
        }
    }
}
