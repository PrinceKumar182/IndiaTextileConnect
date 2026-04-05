package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired(required = false)
    private PlaceRepository placeRepository;

    @Autowired(required = false)
    private ProductRepository productRepository;

    @Autowired(required = false)
    private CartRepository cartRepository;

    @Autowired(required = false)
    private OrderRepository orderRepository;

    @Autowired(required = false)
    private AuditLogRepository auditLog;

    @Autowired(required = false)
    private LoginLogRepository loginLogRepository;

    @Autowired(required = false)
    private UserRepository userRepository;

    @GetMapping
    public String admin(Model model) {
        try {
            model.addAttribute("places", placeRepository != null ? placeRepository.findAll() : List.of());
            model.addAttribute("products", productRepository != null ? productRepository.findBySetsAvailableGreaterThan(0) : List.of());
            model.addAttribute("outOfStockProducts", productRepository != null ? productRepository.findBySetsAvailableLessThanEqual(0) : List.of());
            
            model.addAttribute("loginLogs", loginLogRepository != null ? loginLogRepository.findFirst10ByOrderByLoginTimeDesc() : List.of());
            model.addAttribute("users", userRepository != null ? userRepository.findAll() : List.of());
            model.addAttribute("orders", orderRepository != null ? orderRepository.findAll() : List.of());
            model.addAttribute("auditLogs", auditLog != null ? auditLog.findFirst10ByOrderByTimestampDesc() : List.of());

            // Optimize Cart Enrichment (fetch subset by ID)
            List<Cart> rawCarts = cartRepository != null ? cartRepository.findAll() : List.of();
            // Limit to latest 20 carts for performance
            List<Cart> limitedCarts = rawCarts.size() > 20 ? rawCarts.subList(rawCarts.size() - 20, rawCarts.size()) : rawCarts;
            
            List<Map<String, Object>> enrichedCarts = new ArrayList<>();
            for (Cart cart : limitedCarts) {
                Map<String, Object> enriched = new HashMap<>();
                enriched.put("userId", cart.getUserId());
                enriched.put("quantity", cart.getQuantity());
                
                if (cart.getProductId() != null && productRepository != null) {
                    Product p = productRepository.findById(cart.getProductId()).orElse(null);
                    if (p != null) {
                        enriched.put("productName", p.getName());
                        enriched.put("totalValue", p.getPrice() * cart.getQuantity());
                    } else {
                        enriched.put("productName", "Retired Item");
                        enriched.put("totalValue", 0.0);
                    }
                }
                enrichedCarts.add(enriched);
            }
            model.addAttribute("carts", enrichedCarts);

        } catch (Exception e) {
            model.addAttribute("places", List.of());
            model.addAttribute("products", List.of());
            model.addAttribute("outOfStockProducts", List.of());
            model.addAttribute("loginLogs", List.of());
            model.addAttribute("users", List.of());
            model.addAttribute("carts", List.of());
            model.addAttribute("orders", List.of());
            model.addAttribute("auditLogs", List.of());
            model.addAttribute("error", "Diagnostic Mode: Stability Hardened.");
        }
        return "admin";
    }

    @PostMapping("/addPlace")
    public String addPlace(@RequestParam String name, @RequestParam(required = false) String code, @RequestParam(required = false) String description) {
        try {
            Place place = new Place(name, code, description);
            placeRepository.save(place);
        } catch (Exception e) {
            // Handle errors
        }
        return "redirect:/admin";
    }

    @PostMapping("/deletePlace")
    @org.springframework.transaction.annotation.Transactional
    public String deletePlace(@RequestParam String id) {
        try {
            placeRepository.deleteById(id);
            // Cascade physical wipe
            productRepository.deleteByPlaceId(id);
            auditLog.save(new AuditLog("HUB_DELETED", "ADMIN", "Cascade wiped hub: " + id, null));
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/admin?error=hub_delete_failed";
        }
        return "redirect:/admin?success=hub_deleted";
    }

    @Value("${app.upload.dir:src/main/resources/static/uploads}")
    private String uploadDir;

    @PostMapping("/addProduct")
    public String addProduct(
            @RequestParam String name,
            @RequestParam(required = false, defaultValue = "") String description,
            @RequestParam double price,
            @RequestParam(required = false, defaultValue = "") String imageUrl,
            @RequestParam(required = false) MultipartFile imageFile,
            @RequestParam(required = false, defaultValue = "false") boolean isMostlyBought,
            @RequestParam(required = false, defaultValue = "false") boolean isNewlyAdded,
            @RequestParam String placeId,
            @RequestParam(required = false, defaultValue = "1") int minQuantity,
            @RequestParam int setsAvailable,
            @RequestParam(required = false) MultipartFile[] extraImageFiles,
            @RequestParam(required = false) MultipartFile[] videoFiles) {
        try {
            if (minQuantity <= 0 || setsAvailable <= 0) {
                return "redirect:/admin?error=invalid_quantity_or_stock";
            }

            // Handle image: prefer uploaded file over URL
            String resolvedImageUrl = imageUrl != null && !imageUrl.isBlank() ? imageUrl : "";
            if (imageFile != null && !imageFile.isEmpty()) {
                String originalFilename = imageFile.getOriginalFilename();
                if (originalFilename != null) {
                    String ext = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
                    if (List.of("jpg", "jpeg", "png").contains(ext)) {
                        String uniqueName = UUID.randomUUID() + "." + ext;
                        Path uploadPath = Paths.get(uploadDir);
                        if (!Files.exists(uploadPath)) {
                            Files.createDirectories(uploadPath);
                        }
                        Files.copy(imageFile.getInputStream(), uploadPath.resolve(uniqueName));
                        resolvedImageUrl = "/uploads/" + uniqueName;
                    } else {
                        return "redirect:/admin?error=invalid_image_type";
                    }
                }
            }

            Product product = new Product(name, description, price, resolvedImageUrl, isMostlyBought, isNewlyAdded, placeId, minQuantity, setsAvailable);

            // Handle extra images
            if (extraImageFiles != null) {
                for (MultipartFile file : extraImageFiles) {
                    if (file != null && !file.isEmpty()) {
                        String originalFilename = file.getOriginalFilename();
                        if (originalFilename != null) {
                            String ext = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
                            if (List.of("jpg", "jpeg", "png").contains(ext)) {
                                String uniqueName = UUID.randomUUID() + "." + ext;
                                Path uploadPath = Paths.get(uploadDir);
                                if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
                                Files.copy(file.getInputStream(), uploadPath.resolve(uniqueName));
                                product.getExtraImageUrls().add("/uploads/" + uniqueName);
                            }
                        }
                    }
                }
            }

            // Handle videos
            if (videoFiles != null) {
                for (MultipartFile file : videoFiles) {
                    if (file != null && !file.isEmpty()) {
                        String originalFilename = file.getOriginalFilename();
                        if (originalFilename != null) {
                            String ext = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
                            if (List.of("mp4", "webm", "ogg").contains(ext)) {
                                String uniqueName = UUID.randomUUID() + "." + ext;
                                Path uploadPath = Paths.get(uploadDir);
                                if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
                                Files.copy(file.getInputStream(), uploadPath.resolve(uniqueName));
                                product.getVideoUrls().add("/uploads/" + uniqueName);
                            }
                        }
                    }
                }
            }

            productRepository.save(product);
        } catch (Exception e) {
            System.err.println("Add product error: " + e.getMessage());
            return "redirect:/admin?error=product_add_failed";
        }
        return "redirect:/admin?success=product_added";
    }

    @PostMapping("/deleteProduct")
    public String deleteProduct(@RequestParam String id) {
        try {
            productRepository.deleteById(id);
        } catch (Exception e) {
            // Error handling
        }
        return "redirect:/admin?success=product_deleted";
    }

    @PostMapping("/editProduct")
    public String editProduct(@RequestParam String id, @RequestParam double price, @RequestParam int setsAvailable) {
        try {
            if (setsAvailable < 0 || price < 0) {
                 return "redirect:/admin?error=negative_values_not_allowed";
            }
            Optional<Product> prodOpt = productRepository.findById(id);
            if (prodOpt.isPresent()) {
                Product p = prodOpt.get();
                p.setPrice(price);
                p.setSetsAvailable(setsAvailable);
                productRepository.save(p);
            }
        } catch (Exception e) {
            // Error handling
        }
        return "redirect:/admin?success=product_updated";
    }

    @PostMapping("/restock")
    public String restock(@RequestParam String id, @RequestParam int refillAmount) {
        try {
            if (refillAmount <= 0) return "redirect:/admin?error=invalid_refill";
            Optional<Product> prodOpt = productRepository.findById(id);
            if (prodOpt.isPresent()) {
                Product p = prodOpt.get();
                p.setSetsAvailable(p.getSetsAvailable() + refillAmount);
                productRepository.save(p);
            }
        } catch (Exception e) {
            // Error handling
        }
        return "redirect:/admin?success=product_restocked";
    }
}