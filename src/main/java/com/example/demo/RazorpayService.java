package com.example.demo;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RazorpayService {

    @Value("${razorpay.key:rzp_test_SZCwHDTqlm3cvo}")
    private String razorpayKey;

    @Value("${razorpay.secret:76Ju3OFCaH6Uh7eEYuZmac3H}")
    private String razorpaySecret;

    public Order createPaymentOrder(double amountInBaseCurrency) throws Exception {
        RazorpayClient client = new RazorpayClient(razorpayKey, razorpaySecret);

        JSONObject orderRequest = new JSONObject();
        // Razorpay expects amount in paise (cents)
        orderRequest.put("amount", (int) (amountInBaseCurrency * 100));
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "txn_" + System.currentTimeMillis());

        return client.orders.create(orderRequest);
    }

    public boolean verifySignature(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
        try {
            String payload = razorpayOrderId + "|" + razorpayPaymentId;
            return Utils.verifySignature(payload, razorpaySignature, razorpaySecret);
        } catch (Exception e) {
            return false;
        }
    }

    public String getExpectedKey() {
        return razorpayKey;
    }
}
