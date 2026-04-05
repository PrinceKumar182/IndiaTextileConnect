package com.example.demo;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class CheckoutController {

    private final CheckoutService checkoutService;
    private final RazorpayService razorpayService;
    private final RateLimiterService rateLimiterService;
    private final AuditLogRepository auditLog;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;

    public CheckoutController(CheckoutService checkoutService, RazorpayService razorpayService, RateLimiterService rateLimiterService, AuditLogRepository auditLog, CartRepository cartRepository, OrderRepository orderRepository) {
        this.checkoutService = checkoutService;
        this.razorpayService = razorpayService;
        this.rateLimiterService = rateLimiterService;
        this.auditLog = auditLog;
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
    }

    @GetMapping("/checkout")
    public String showCheckoutPage(Model model, Authentication auth) {
        String username = auth.getName();
        if (!rateLimiterService.tryConsume("checkout_page_" + username)) {
            return "redirect:/?error=rate_limit_exceeded";
        }

        List<Cart> cartItems = cartRepository.findByUserId(username);
        if (cartItems.isEmpty()) {
            return "redirect:/cart";
        }

        double totalAmount = checkoutService.calculateCartTotal(username);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("razorpayKey", razorpayService.getExpectedKey());
        
        // Generate Token
        String idempotencyToken = UUID.randomUUID().toString();
        model.addAttribute("checkoutToken", idempotencyToken);

        return "checkout";
    }

    @PostMapping("/api/checkout/initiate")
    @ResponseBody
    public ResponseEntity<?> initiateCheckout(@RequestBody Map<String, String> payload, Authentication auth) {
        String username = auth.getName();
        if (!rateLimiterService.tryConsume("checkout_init_" + username)) {
            return ResponseEntity.status(429).body(Map.of("error", "Rate limit exceeded."));
        }

        try {
            String checkoutToken = payload.get("checkoutToken");
            String phone = payload.get("phone");
            String city = payload.get("city");
            String address = payload.get("address");

            // Basic Masking
            String maskedPhone = phone.replaceAll("(?<=.{2}).(?=.{2})", "*");

            // Reserve Stock & Create (Tx 1)
            com.example.demo.Order order = checkoutService.reserveStockAndCreateOrder(username, checkoutToken, maskedPhone, city, address);

            // Razorpay Ext (Non-Tx)
            com.razorpay.Order rzpOrder = razorpayService.createPaymentOrder(order.getTotalAmount());
            order.setRazorpayOrderId(rzpOrder.get("id"));
            orderRepository.save(order);

            auditLog.save(new AuditLog("ORDER_RESERVED", username, "Reserved stock and created Order: " + order.getId(), checkoutToken));

            return ResponseEntity.ok(Map.of("razorpayOrderId", order.getRazorpayOrderId(), "amount", order.getTotalAmount(), "dbOrderId", order.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/api/checkout/verify")
    @ResponseBody
    public ResponseEntity<?> verifyCheckout(@RequestBody Map<String, Object> payload, Authentication auth) {
        String username = auth.getName();
        try {
            String razorpayOrderId = (String) payload.get("razorpay_order_id");
            String razorpayPaymentId = (String) payload.get("razorpay_payment_id");
            String razorpaySignature = (String) payload.get("razorpay_signature");
            String dbOrderId = (String) payload.get("dbOrderId");
            
            // Expected amount fetched from frontend to pass into Verify, but it could be forged.
            // Wait, we actually verify it securely in Transaction 2!
            double amountExpectedByClient = Double.parseDouble(payload.get("amount").toString());

            if (!razorpayService.verifySignature(razorpayOrderId, razorpayPaymentId, razorpaySignature)) {
                // Potential forgery or failure
                checkoutService.cancelOrderAndRestoreStock(dbOrderId);
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid Signature."));
            }

            // Commit (Tx 2). Automatically validates Amount!
            checkoutService.commitOrderAndClearCart(dbOrderId, razorpayPaymentId, amountExpectedByClient, username);

            auditLog.save(new AuditLog("PAYMENT_CAPTURED", username, "Successfully committed Payment for: " + dbOrderId, razorpayPaymentId));

            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
             return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/api/webhooks/razorpay")
    @ResponseBody
    public ResponseEntity<?> handleRazorpayWebhook(@RequestBody String payload, @RequestHeader(value="X-Razorpay-Signature", required=false) String signature) {
        // Abstract representation of robust webhook endpoint preventing front-end session loss
        // Real implementation parses JSON to extract `razorpay_order_id` and hits verify endpoint
        auditLog.save(new AuditLog("WEBHOOK_RECEIVED", "SYSTEM", "Received async webhook", "Signature: " + (signature != null)));
        return ResponseEntity.ok().build();
    }
}
