package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Optional;

@Controller
public class AuthController {

    @Autowired(required = false)
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired(required = false)
    private FirebaseService firebaseService;

    private static final java.util.List<String> TEST_NUMBERS = java.util.Arrays.asList("9166015342", "8287813797");

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "forgot-password";
    }

    /**
     * Registration endpoint.
     * Accepts the Firebase ID token (after client-side OTP verification),
     * verifies it server-side, then creates the user.
     * Username = phone number (10 digits).
     */
    @PostMapping("/register")
    public String register(
            @RequestParam String phone,
            @RequestParam String password,
            @RequestParam(required = false) String firebaseIdToken) {
        try {
            // Validate phone format
            if (phone == null || !phone.matches("\\d{10}")) {
                return "redirect:/signup?error=invalid_phone";
            }
            if (password == null || password.length() < 6) {
                return "redirect:/signup?error=weak_password";
            }
            if (userRepository == null) {
                return "redirect:/signup?error=db_unavailable";
            }

            // Verify OTP via Firebase
            boolean isTestPhone = TEST_NUMBERS.contains(phone);
            boolean isTestBypass = isTestPhone && ("TEST_BYPASS".equals(firebaseIdToken) || firebaseIdToken == null || firebaseIdToken.isBlank());

            if (isTestBypass) {
                System.out.println("[TEST] Bypassing Firebase check for registration: " + phone);
            } else if (firebaseService != null && firebaseIdToken != null && !firebaseIdToken.isBlank()) {
                String verifiedPhone = firebaseService.verifyIdTokenAndGetPhone(firebaseIdToken);
                if (!phone.equals(verifiedPhone)) {
                    return "redirect:/signup?error=otp_mismatch";
                }
            } else {
                // Reject registration if Firebase is unavailable or no token given
                return "redirect:/signup?error=otp_required";
            }

            // Check if phone already registered (use findFirstByPhone to handle duplicates gracefully)
            if (userRepository.findFirstByPhone(phone).isPresent()) {
                return "redirect:/signup?error=phone_exists";
            }

            // Store: username = phone (used by Spring Security for login)
            User user = new User(phone, passwordEncoder.encode(password), "USER", phone);
            userRepository.save(user);
        } catch (Exception e) {
            System.err.println("Registration error: " + e.getMessage());
            return "redirect:/signup?error=server_error";
        }
        return "redirect:/login?registered=true";
    }

    /**
     * Password Reset endpoint.
     * Client verifies OTP via Firebase, sends token + new password here.
     */
    @PostMapping("/reset-password")
    public String resetPassword(
            @RequestParam String phone,
            @RequestParam String newPassword,
            @RequestParam(required = false) String firebaseIdToken) {
        try {
            if (phone == null || !phone.matches("\\d{10}")) {
                return "redirect:/forgot-password?error=invalid_phone";
            }
            if (newPassword == null || newPassword.length() < 6) {
                return "redirect:/forgot-password?error=weak_password";
            }
            if (userRepository == null) {
                return "redirect:/forgot-password?error=db_unavailable";
            }

            // Verify OTP via Firebase
            boolean isTestPhone = TEST_NUMBERS.contains(phone);
            boolean isTestBypass = isTestPhone && ("TEST_BYPASS".equals(firebaseIdToken) || firebaseIdToken == null || firebaseIdToken.isBlank());

            if (isTestBypass) {
                System.out.println("[TEST] Bypassing Firebase check for password reset: " + phone);
            } else if (firebaseService != null && firebaseIdToken != null && !firebaseIdToken.isBlank()) {
                String verifiedPhone = firebaseService.verifyIdTokenAndGetPhone(firebaseIdToken);
                if (verifiedPhone == null || !phone.equals(verifiedPhone)) {
                    return "redirect:/forgot-password?error=otp_mismatch";
                }
            } else {
                return "redirect:/forgot-password?error=otp_required";
            }

            // Find user by phone and update password (use findFirstByPhone to handle any duplicates)
            Optional<User> userOpt = userRepository.findFirstByPhone(phone);
            if (userOpt.isEmpty()) {
                return "redirect:/forgot-password?error=not_registered";
            }

            User user = userOpt.get();
            System.out.println("[DEBUG] Found user: " + user.getUsername() + ", phone: " + user.getPhone());
            System.out.println("[DEBUG] Old password hash prefix: " + (user.getPassword() != null && user.getPassword().length() > 10 ? user.getPassword().substring(0, 10) : "null"));
            String encoded = passwordEncoder.encode(newPassword);
            user.setPassword(encoded);
            userRepository.save(user);
            System.out.println("✅ Password successfully updated in DB for phone: " + phone);
            System.out.println("[DEBUG] New encoded password prefix: " + (encoded != null && encoded.length() > 10 ? encoded.substring(0, 10) : "null"));
            // Verify the save worked by reloading from DB
            Optional<User> reloaded = userRepository.findFirstByPhone(phone);
            if (reloaded.isPresent()) {
                boolean matchOk = passwordEncoder.matches(newPassword, reloaded.get().getPassword());
                System.out.println("[DEBUG] Reload verify - password matches: " + matchOk);
            }

        } catch (Exception e) {
            System.err.println("❌ Password reset error: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/forgot-password?error=server_error&details=" + java.net.URLEncoder.encode(e.getClass().getSimpleName() + ":" + e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
        }
        return "redirect:/login?reset=true";
    }
}