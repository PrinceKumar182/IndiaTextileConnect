package com.example.demo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document(collection = "orders")
public class Order {

    @Id
    private String id;
    private String userId;
    private double totalAmount;
    private OrderStatus status;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    
    // Uniquely bind this order checkout to prevent double-processing identical requests
    private String idempotencyToken;

    // Secure Data Minimized representation
    private String maskedPhone;
    private String city;
    private String addressLineEncrypted; // Raw placeholder for now

    private List<OrderItem> items;
    private Date createdAt;

    public Order() {
    }

    public Order(String userId, double totalAmount, OrderStatus status, String idempotencyToken, String maskedPhone, String city, String address, List<OrderItem> items) {
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.status = status;
        this.idempotencyToken = idempotencyToken;
        this.maskedPhone = maskedPhone;
        this.city = city;
        this.addressLineEncrypted = address;
        this.items = items;
        this.createdAt = new Date();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public String getRazorpayOrderId() { return razorpayOrderId; }
    public void setRazorpayOrderId(String razorpayOrderId) { this.razorpayOrderId = razorpayOrderId; }
    public String getRazorpayPaymentId() { return razorpayPaymentId; }
    public void setRazorpayPaymentId(String razorpayPaymentId) { this.razorpayPaymentId = razorpayPaymentId; }
    public String getIdempotencyToken() { return idempotencyToken; }
    public void setIdempotencyToken(String idempotencyToken) { this.idempotencyToken = idempotencyToken; }
    public String getMaskedPhone() { return maskedPhone; }
    public void setMaskedPhone(String maskedPhone) { this.maskedPhone = maskedPhone; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getAddressLineEncrypted() { return addressLineEncrypted; }
    public void setAddressLineEncrypted(String addressLineEncrypted) { this.addressLineEncrypted = addressLineEncrypted; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public static class OrderItem {
        private String productId;
        private String productName;
        private int quantity;
        private double capturedPrice;

        public OrderItem() {}
        public OrderItem(String productId, String productName, int quantity, double capturedPrice) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.capturedPrice = capturedPrice;
        }

        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public double getCapturedPrice() { return capturedPrice; }
        public void setCapturedPrice(double capturedPrice) { this.capturedPrice = capturedPrice; }
    }
}
