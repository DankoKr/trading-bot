package dankok.trading212.auto_trading_bot.controllers;

import dankok.trading212.auto_trading_bot.dtos.User;
import dankok.trading212.auto_trading_bot.dtos.AuthResponse;
import dankok.trading212.auto_trading_bot.dtos.LoginRequest;
import dankok.trading212.auto_trading_bot.dtos.RegisterRequest;
import dankok.trading212.auto_trading_bot.services.UserService;
import dankok.trading212.auto_trading_bot.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        try {
            User user = userService.authenticateUser(request.getUsername(), request.getPassword());
            
            if (user != null) {
                String token = jwtUtil.generateToken(user.getUsername(), user.getId());
                
                String tokenHash = String.valueOf(token.hashCode());
                long expirationMillis = System.currentTimeMillis() + 86400000; // 24 hours
                userService.saveUserSession(user.getId(), tokenHash, expirationMillis);
                
                return new AuthResponse(true, "Login successful", token, user);
            } else {
                return new AuthResponse(false, "Invalid username or password");
            }
        } catch (Exception e) {
            return new AuthResponse(false, "Login failed: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request) {
        try {
            User user = userService.createUser(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getFirstName(),
                request.getLastName()
            );
            
            String token = jwtUtil.generateToken(user.getUsername(), user.getId());
            
            String tokenHash = String.valueOf(token.hashCode());
            long expirationMillis = System.currentTimeMillis() + 86400000; // 24 hours
            userService.saveUserSession(user.getId(), tokenHash, expirationMillis);
            
            return new AuthResponse(true, "Registration successful", token, user);
        } catch (Exception e) {
            return new AuthResponse(false, "Registration failed: " + e.getMessage());
        }
    }

    @PostMapping("/logout")
    public AuthResponse logout(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String username = jwtUtil.getUsernameFromToken(token);
                Integer userId = jwtUtil.getUserIdFromToken(token);
                
                if (username != null && userId != null) {
                    String tokenHash = String.valueOf(token.hashCode());
                    userService.invalidateSession(userId, tokenHash);
                }
            }
            return new AuthResponse(true, "Logout successful");
        } catch (Exception e) {
            return new AuthResponse(false, "Logout failed: " + e.getMessage());
        }
    }

    @GetMapping("/profile")
    public AuthResponse getProfile(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String username = jwtUtil.getUsernameFromToken(token);
                
                if (username != null && !jwtUtil.isTokenExpired(token)) {
                    User user = userService.getUserByUsername(username);
                    if (user != null) {
                        return new AuthResponse(true, "Profile retrieved successfully", null, user);
                    }
                }
            }
            return new AuthResponse(false, "Invalid or expired token");
        } catch (Exception e) {
            return new AuthResponse(false, "Failed to get profile: " + e.getMessage());
        }
    }
}