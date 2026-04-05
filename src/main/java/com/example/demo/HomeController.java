package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@Controller
public class HomeController {

    @Autowired(required = false)
    private PlaceRepository placeRepository;

    @Autowired(required = false)
    private ProductRepository productRepository;

    @Autowired(required = false)
    private VisitorRepository visitorRepository;

    @Autowired(required = false)
    private CartRepository cartRepository;

    @GetMapping("/")
    public String home(Model model) {
        try {
            model.addAttribute("places", placeRepository != null ? placeRepository.findAll() : List.of());
            model.addAttribute("mostlyBought", productRepository != null ? productRepository.findByIsMostlyBoughtTrueAndSetsAvailableGreaterThan(0) : List.of());
            model.addAttribute("newlyAdded", productRepository != null ? productRepository.findByIsNewlyAddedTrueAndSetsAvailableGreaterThan(0) : List.of());
        } catch (Exception e) {
            model.addAttribute("places", List.of());
            model.addAttribute("mostlyBought", List.of());
            model.addAttribute("newlyAdded", List.of());
            model.addAttribute("error", "Database connection issue.");
        }
        return "home";
    }

    @PostMapping("/visitor")
    public String saveVisitor(@RequestParam String phone, Model model) {
        try {
            if (phone == null || !phone.matches("\\d{10}")) {
                model.addAttribute("error", "Invalid phone number. Must be 10 digits.");
                return home(model);
            }
            if (visitorRepository != null) {
                Visitor visitor = new Visitor(phone);
                visitorRepository.save(visitor);
            }
        } catch (Exception e) {
            model.addAttribute("error", "Database error.");
        }
        return "redirect:/?success=visitor_saved";
    }

    @GetMapping("/products")
    public String products(@RequestParam(required = false) String placeId, Model model) {
        try {
            if (placeId == null || placeId.isEmpty()) {
                model.addAttribute("products", productRepository != null ? productRepository.findBySetsAvailableGreaterThan(0) : List.of());
                model.addAttribute("place", null);
            } else {
                model.addAttribute("products", productRepository != null ? productRepository.findByPlaceIdAndSetsAvailableGreaterThan(placeId, 0) : List.of());
                model.addAttribute("place", placeRepository != null ? placeRepository.findById(placeId).orElse(null) : null);
            }
        } catch (Exception e) {
            model.addAttribute("products", List.of());
            model.addAttribute("place", null);
            model.addAttribute("error", "Database connection issue.");
        }
        return "products";
    }

    @GetMapping("/product")
    public String product(@RequestParam String id, Model model) {
        try {
            model.addAttribute("product", productRepository != null ? productRepository.findById(id).orElse(null) : null);
        } catch (Exception e) {
            model.addAttribute("product", null);
            model.addAttribute("error", "Database connection issue.");
        }
        return "product";
    }

    @GetMapping("/search")
    public String search(@RequestParam String query, Model model) {
        try {
            model.addAttribute("products", productRepository != null ? productRepository.findByNameContainingIgnoreCaseAndSetsAvailableGreaterThan(query, 0) : List.of());
            model.addAttribute("searchQuery", query);
        } catch (Exception e) {
            model.addAttribute("products", List.of());
            model.addAttribute("error", "Database connection issue.");
        }
        return "products";
    }

    @PostMapping("/addToCart")
    public String addToCart(@RequestParam String productId, @RequestParam(defaultValue = "1") int quantity, @RequestParam(required = false, defaultValue = "false") boolean buyNow) {
        try {
            String userId = getCurrentUserId();
            if (cartRepository != null) {
                // Merge: if product already in cart, increment quantity instead of creating duplicate
                Optional<Cart> existing = cartRepository.findByUserIdAndProductId(userId, productId);
                if (existing.isPresent()) {
                    Cart c = existing.get();
                    c.setQuantity(c.getQuantity() + quantity);
                    cartRepository.save(c);
                } else {
                    cartRepository.save(new Cart(userId, productId, quantity));
                }
            }
            if (buyNow) {
                return "redirect:/cart";
            }
        } catch (Exception e) {
            // Error handling
        }
        return "redirect:/product?id=" + productId + "&added=true";
    }

    @PostMapping("/buyNow")
    public String buyNow(@RequestParam String productId, Authentication auth) {
        try {
            String username = auth.getName();
            if (cartRepository != null) {
                Optional<Cart> existingCartLine = cartRepository.findByUserIdAndProductId(username, productId);
                if (existingCartLine.isPresent()) {
                    Cart c = existingCartLine.get();
                    c.setQuantity(c.getQuantity() + 1);
                    cartRepository.save(c);
                } else {
                    cartRepository.save(new Cart(username, productId, 1));
                }
            }
        } catch (Exception e) {}
        return "redirect:/checkout";
    }

    @GetMapping("/cart")
    public String viewCart(Model model) {
        try {
            String userId = getCurrentUserId();
            if (cartRepository == null) {
                model.addAttribute("cartItems", List.of());
                model.addAttribute("grandTotal", 0.0);
                return "cart";
            }

            List<Cart> rawCarts = cartRepository.findByUserId(userId);
            
            // --- Background State Hardening: Merge Duplicates ---
            Map<String, Cart> uniqueProducts = new HashMap<>();
            List<Cart> toDelete = new ArrayList<>();
            for (Cart c : rawCarts) {
                if (uniqueProducts.containsKey(c.getProductId())) {
                    Cart existing = uniqueProducts.get(c.getProductId());
                    existing.setQuantity(existing.getQuantity() + c.getQuantity());
                    toDelete.add(c); // Mark duplicate for deletion
                } else {
                    uniqueProducts.put(c.getProductId(), c);
                }
            }
            if (!toDelete.isEmpty()) {
                cartRepository.deleteAll(toDelete);
                for (Cart c : uniqueProducts.values()) {
                    cartRepository.save(c); // Update the consolidated records
                }
                rawCarts = new ArrayList<>(uniqueProducts.values()); // Refresh list for view
            }
            // ----------------------------------------------------

            List<Map<String, Object>> enrichedCarts = new ArrayList<>();
            double grandTotal = 0.0;

            for (Cart cart : rawCarts) {
                Map<String, Object> enriched = new HashMap<>();
                enriched.put("id", cart.getId());
                enriched.put("productId", cart.getProductId());
                enriched.put("quantity", cart.getQuantity());

                Product p = productRepository != null ? productRepository.findById(cart.getProductId()).orElse(null) : null;
                if (p != null) {
                    enriched.put("productName", p.getName());
                    enriched.put("price", p.getPrice());
                    double itemTotal = p.getPrice() * cart.getQuantity();
                    enriched.put("itemTotal", itemTotal);
                    grandTotal += itemTotal;
                } else {
                    enriched.put("productName", "Unknown/Deleted Product");
                    enriched.put("price", 0.0);
                    enriched.put("itemTotal", 0.0);
                }
                enrichedCarts.add(enriched);
            }
            model.addAttribute("cartItems", enrichedCarts);
            model.addAttribute("grandTotal", grandTotal);
        } catch (Exception e) {
            model.addAttribute("cartItems", List.of());
            model.addAttribute("grandTotal", 0.0);
            model.addAttribute("error", "Unable to load cart.");
        }
        return "cart";
    }

    @PostMapping("/removeFromCart")
    public String removeFromCart(@RequestParam String cartId) {
        try {
            if (cartRepository != null) {
                cartRepository.deleteById(cartId);
            }
        } catch (Exception e) {
            // Error handling
        }
        return "redirect:/cart";
    }

    @PostMapping("/checkout")
    public String checkout() {
        try {
            String userId = getCurrentUserId();
            if (cartRepository != null) {
                List<Cart> items = cartRepository.findByUserId(userId);
                cartRepository.deleteAll(items);
            }
        } catch (Exception e) {
            // Error handling
        }
        return "redirect:/?success=order_placed";
    }

    // REST endpoint for AJAX cart badge count
    @GetMapping("/api/cart/count")
    @ResponseBody
    public Map<String, Object> cartCount() {
        Map<String, Object> result = new HashMap<>();
        try {
            String userId = getCurrentUserId();
            if (cartRepository != null) {
                List<Cart> carts = cartRepository.findByUserId(userId);
                int totalItems = carts.stream().mapToInt(Cart::getQuantity).sum();
                result.put("count", totalItems);
            } else {
                result.put("count", 0);
            }
        } catch (Exception e) {
            result.put("count", 0);
        }
        return result;
    }

    // REST endpoint for AJAX cart quantity update
    @PostMapping("/api/cart/updateQuantity")
    @ResponseBody
    public Map<String, Object> updateCartQuantity(@RequestParam String productId, @RequestParam int quantity) {
        Map<String, Object> result = new HashMap<>();
        try {
            String userId = getCurrentUserId();
            if (cartRepository != null) {
                Optional<Cart> existing = cartRepository.findByUserIdAndProductId(userId, productId);
                if (existing.isPresent()) {
                    if (quantity <= 0) {
                        cartRepository.delete(existing.get());
                        result.put("removed", true);
                    } else {
                        Cart c = existing.get();
                        c.setQuantity(quantity);
                        cartRepository.save(c);
                        result.put("removed", false);
                    }
                }
            }
            result.put("success", true);
            // Return updated total count
            List<Cart> carts = cartRepository != null ? cartRepository.findByUserId(userId) : List.of();
            result.put("cartCount", carts.stream().mapToInt(Cart::getQuantity).sum());
        } catch (Exception e) {
            result.put("success", false);
        }
        return result;
    }

    private String getCurrentUserId() {
        return org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
    }
}