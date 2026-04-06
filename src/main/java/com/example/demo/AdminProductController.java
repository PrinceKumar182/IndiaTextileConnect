package com.example.demo;

import com.example.demo.dto.ProductDto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping("/addProduct")
    public String addProduct(@Valid ProductDto productDto, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/admin";
        }

        try {
            String primaryImageUrl = "";
            if (productDto.getImageFile() != null && !productDto.getImageFile().isEmpty()) {
                primaryImageUrl = fileStorageService.saveFile(productDto.getImageFile(), List.of("jpg", "jpeg", "png", "webp"));
            }

            Product product = new Product(
                    productDto.getName(),
                    productDto.getDescription(),
                    productDto.getPrice(),
                    primaryImageUrl,
                    productDto.isMostlyBought(),
                    productDto.isNewlyAdded(),
                    productDto.getPlaceId(),
                    productDto.getCategoryId(),
                    productDto.getMinQuantity(),
                    productDto.getSetsAvailable()
            );

            // Extra images
            if (productDto.getExtraImageFiles() != null) {
                for (MultipartFile file : productDto.getExtraImageFiles()) {
                    if (file != null && !file.isEmpty()) {
                        String fileUrl = fileStorageService.saveFile(file, List.of("jpg", "jpeg", "png", "webp"));
                        product.getExtraImageUrls().add(fileUrl);
                    }
                }
            }

            // Videos
            if (productDto.getVideoFiles() != null) {
                for (MultipartFile file : productDto.getVideoFiles()) {
                    if (file != null && !file.isEmpty()) {
                        String fileUrl = fileStorageService.saveFile(file, List.of("mp4", "webm", "ogg"));
                        product.getVideoUrls().add(fileUrl);
                    }
                }
            }

            productRepository.save(product);
            redirectAttributes.addFlashAttribute("success", "Product added successfully.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to add product.");
        }
        return "redirect:/admin";
    }

    @PostMapping("/deleteProduct")
    public String deleteProduct(@RequestParam String id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Product> prodOpt = productRepository.findById(id);
            if (prodOpt.isPresent()) {
                Product p = prodOpt.get();
                // Cleanup files
                fileStorageService.deleteFile(p.getImageUrl());
                for (String url : p.getExtraImageUrls()) fileStorageService.deleteFile(url);
                for (String url : p.getVideoUrls()) fileStorageService.deleteFile(url);
                
                productRepository.deleteById(id);
                redirectAttributes.addFlashAttribute("success", "Product deleted.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete product.");
        }
        return "redirect:/admin";
    }

    @PostMapping("/editProduct")
    public String editProduct(@RequestParam String id, @RequestParam double price, @RequestParam int setsAvailable, RedirectAttributes redirectAttributes) {
        try {
            if (setsAvailable < 0 || price < 0) {
                 redirectAttributes.addFlashAttribute("error", "Negative values not allowed.");
                 return "redirect:/admin";
            }
            Optional<Product> prodOpt = productRepository.findById(id);
            if (prodOpt.isPresent()) {
                Product p = prodOpt.get();
                p.setPrice(price);
                p.setSetsAvailable(setsAvailable);
                productRepository.save(p);
                redirectAttributes.addFlashAttribute("success", "Product updated.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update product.");
        }
        return "redirect:/admin";
    }

    @PostMapping("/restock")
    public String restock(@RequestParam String id, @RequestParam int refillAmount, RedirectAttributes redirectAttributes) {
        try {
            if (refillAmount <= 0) {
                redirectAttributes.addFlashAttribute("error", "Invalid refill amount.");
                return "redirect:/admin";
            }
            Optional<Product> prodOpt = productRepository.findById(id);
            if (prodOpt.isPresent()) {
                Product p = prodOpt.get();
                p.setSetsAvailable(p.getSetsAvailable() + refillAmount);
                productRepository.save(p);
                redirectAttributes.addFlashAttribute("success", "Stock replenished.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to restock.");
        }
        return "redirect:/admin";
    }
}
