package com.example.demo;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CheckoutService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;

    public CheckoutService(OrderRepository orderRepository, ProductRepository productRepository, CartRepository cartRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
    }

    @Transactional
    public com.example.demo.Order reserveStockAndCreateOrder(String userId, String idempotencyToken, String maskedPhone, String city, String encryptedAddress) throws Exception {
        // Idempotency check
        if (orderRepository.findByIdempotencyToken(idempotencyToken) != null) {
            throw new Exception("Order already placed.");
        }

        List<Cart> cartItems = cartRepository.findByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new Exception("Cart is empty.");
        }

        double totalAmount = 0;
        List<com.example.demo.Order.OrderItem> orderItems = new ArrayList<>();

        // Validate and reserve stock
        for (Cart item : cartItems) {
            Optional<Product> pOpt = productRepository.findById(item.getProductId());
            if (pOpt.isEmpty()) {
                throw new Exception("Product " + item.getProductId() + " not found.");
            }
            Product p = pOpt.get();
            if (p.getSetsAvailable() < item.getQuantity()) {
                throw new Exception("Out of stock for: " + p.getName());
            }

            // Reserve stock
            p.setSetsAvailable(p.getSetsAvailable() - item.getQuantity());
            productRepository.save(p);

            // Accumulate price
            double capturedPrice = p.getPrice();
            totalAmount += capturedPrice * item.getQuantity();

            orderItems.add(new com.example.demo.Order.OrderItem(p.getId(), p.getName(), item.getQuantity(), capturedPrice));
        }

        // Create Order (PAYMENT_PENDING)
        com.example.demo.Order order = new com.example.demo.Order(userId, totalAmount, OrderStatus.PAYMENT_PENDING, idempotencyToken, maskedPhone, city, encryptedAddress, orderItems);
        return orderRepository.save(order);
    }

    @Transactional
    public void commitOrderAndClearCart(String orderId, String paymentId, double verifiedAmount, String userId) throws Exception {
        Optional<com.example.demo.Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new Exception("Order not found.");
        }
        
        com.example.demo.Order order = orderOpt.get();
        if (order.getStatus() != OrderStatus.PAYMENT_PENDING) {
            return; // Already processed
        }

        // Amount safeguard Verification
        if (Math.abs(order.getTotalAmount() - verifiedAmount) > 0.01) {
            // Restore inventory because attacker tried to bypass amount!
            restoreInventory(order);
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            throw new Exception("Payment amount mismatch. Order cancelled natively.");
        }

        // Commit order
        order.setStatus(OrderStatus.PAID);
        order.setRazorpayPaymentId(paymentId);
        orderRepository.save(order);

        // Clear cart
        cartRepository.deleteByUserId(userId);
    }

    @Transactional
    public void cancelOrderAndRestoreStock(String orderId) {
        Optional<com.example.demo.Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            com.example.demo.Order order = orderOpt.get();
            if (order.getStatus() == OrderStatus.PAYMENT_PENDING) {
                restoreInventory(order);
                order.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(order);
            }
        }
    }

    private void restoreInventory(com.example.demo.Order order) {
        for (com.example.demo.Order.OrderItem item : order.getItems()) {
            Optional<Product> pOpt = productRepository.findById(item.getProductId());
            if (pOpt.isPresent()) {
                Product p = pOpt.get();
                p.setSetsAvailable(p.getSetsAvailable() + item.getQuantity());
                productRepository.save(p);
            }
        }
    }

    public double calculateCartTotal(String userId) {
        List<Cart> cartItems = cartRepository.findByUserId(userId);
        double total = 0;
        for (Cart item : cartItems) {
            Optional<Product> pOpt = productRepository.findById(item.getProductId());
            if (pOpt.isPresent()) {
                total += pOpt.get().getPrice() * item.getQuantity();
            }
        }
        return total;
    }
}
