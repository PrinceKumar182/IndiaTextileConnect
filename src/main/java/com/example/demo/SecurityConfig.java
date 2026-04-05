package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import java.util.Optional;

import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired(required = false)
    private UserRepository userRepository;

    @Autowired
    private CustomAuthenticationSuccessHandler successHandler;

    @Autowired(required = false)
    private LoginAttemptService loginAttemptService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(org.springframework.security.config.Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/", "/home", "/login", "/signup", "/visitor", "/register", "/error", "/css/**", "/js/**", "/images/**", "/uploads/**").permitAll()
                .requestMatchers("/forgot-password", "/reset-password").permitAll()
                .requestMatchers("/products", "/product", "/search").permitAll()
                .requestMatchers("/api/webhooks/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/addToCart", "/removeFromCart", "/buyNow", "/api/cart/count", "/api/cart/updateQuantity").authenticated()
                .requestMatchers("/cart", "/cart/**", "/checkout", "/checkout/**", "/api/checkout/**").authenticated()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(successHandler)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/logout", "POST"))
                .logoutSuccessUrl("/")
                .permitAll()
            );
        return http.build();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            if (loginAttemptService != null && loginAttemptService.isBlocked(username)) {
                throw new org.springframework.security.authentication.LockedException("User " + username + " is blocked due to excessive login attempts");
            }
            if (userRepository == null) {
                throw new UsernameNotFoundException("Database not available");
            }

            // Strategy 1: Look up by phone number (for regular users)
            System.out.println("🔍 Security Login Attempt for: " + username);
            Optional<User> userByPhone = userRepository.findFirstByPhone(username);
            if (userByPhone.isPresent()) {
                User u = userByPhone.get();
                System.out.println("✅ Found user by phone: " + u.getUsername() + " (Role: " + u.getRole() + ")");
                return org.springframework.security.core.userdetails.User
                    .withUsername(u.getUsername())
                    .password(u.getPassword())
                    .roles(u.getRole())
                    .build();
            }

            // Strategy 2: Fall back to username lookup
            Optional<User> userByUsername = userRepository.findByUsername(username);
            if (userByUsername.isPresent()) {
                User u = userByUsername.get();
                System.out.println("✅ Found user by username: " + u.getUsername() + " (Role: " + u.getRole() + ")");
                return org.springframework.security.core.userdetails.User
                    .withUsername(u.getUsername())
                    .password(u.getPassword())
                    .roles(u.getRole())
                    .build();
            }

            System.out.println("❌ User not found in DB: " + username);
            throw new UsernameNotFoundException("User not found: " + username);
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}