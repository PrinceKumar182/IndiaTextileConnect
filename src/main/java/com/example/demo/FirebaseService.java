package com.example.demo;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.stereotype.Service;

@Service
public class FirebaseService {

    /**
     * Verifies a Firebase ID Token received from the client after OTP verification.
     * Returns the phone number encoded in the token, or null if invalid.
     */
    public String verifyIdTokenAndGetPhone(String idToken) {
        try {
            if (idToken == null || idToken.isBlank() || FirebaseApp.getApps().isEmpty()) {
                return null;
            }
            FirebaseToken decoded = FirebaseAuth.getInstance().verifyIdToken(idToken);
            // For phone auth, the phone number is stored in the phone_number claim
            String phone = decoded.getClaims().containsKey("phone_number")
                    ? (String) decoded.getClaims().get("phone_number")
                    : null;
            if (phone != null && phone.startsWith("+91")) {
                // Strip country code to get 10-digit number
                phone = phone.substring(3);
            }
            return phone;
        } catch (Exception e) {
            System.err.println("Firebase token verification failed: " + e.getMessage());
            return null;
        }
    }
}
