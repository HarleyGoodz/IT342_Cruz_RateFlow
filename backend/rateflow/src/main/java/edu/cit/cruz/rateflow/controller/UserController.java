package edu.cit.cruz.rateflow.controller;

import java.util.Optional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // Add this import
import org.springframework.mail.SimpleMailMessage; // Add this import
import org.springframework.mail.javamail.JavaMailSender; // Add this import

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.cit.cruz.rateflow.entity.Role;
import edu.cit.cruz.rateflow.entity.User;
import edu.cit.cruz.rateflow.repository.UserRepository;
import edu.cit.cruz.rateflow.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping(path = "/api/auth")
//@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class UserController {

    @Autowired
    private UserService userv;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private edu.cit.cruz.rateflow.service.NotificationService notificationService;

    // Forgot Password - Request reset
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            // Don't reveal that email doesn't exist for security
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "If an account exists with that email, you will receive a password reset link."
            ));
        }
        
        User user = userOpt.get();
        
        // Generate reset token
        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(1)); // Token valid for 1 hour
        userRepository.save(user);
        
        // Send email
        try {
            String resetLink = "http://localhost:3000/reset-password?token=" + resetToken;
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Password Reset Request - RateFlow");
            message.setText("Hello " + user.getUsername() + ",\n\n"
                + "You requested to reset your password. Click the link below to reset it:\n\n"
                + resetLink + "\n\n"
                + "This link will expire in 1 hour.\n\n"
                + "If you did not request this, please ignore this email.\n\n"
                + "Best regards,\nRateFlow Team");
            
            mailSender.send(message);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Password reset link has been sent to your email."
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Failed to send reset email. Please try again later."
            ));
        }
    }
    
    // Reset Password - Verify token and update password
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");
        
        Optional<User> userOpt = userRepository.findByResetToken(token);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Invalid or expired reset token."
            ));
        }
        
        User user = userOpt.get();
        
        // Check if token is expired
        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Reset token has expired. Please request a new one."
            ));
        }
        
        // Update password
        String hashedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(hashedPassword);
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
        
        // Send confirmation email
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("Password Reset Successful - RateFlow");
            message.setText("Hello " + user.getUsername() + ",\n\n"
                + "Your password has been successfully reset.\n\n"
                + "If you did not perform this action, please contact support immediately.\n\n"
                + "Best regards,\nRateFlow Team");
            mailSender.send(message);
        } catch (Exception e) {
            // Don't fail if confirmation email fails
            e.printStackTrace();
        }
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Password has been reset successfully. You can now login with your new password."
        ));
    }
    
    // Validate reset token
    @GetMapping("/validate-reset-token")
    public ResponseEntity<?> validateResetToken(@RequestParam String token) {
        Optional<User> userOpt = userRepository.findByResetToken(token);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "valid", false,
                "message", "Invalid token"
            ));
        }
        
        User user = userOpt.get();
        
        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body(Map.of(
                "valid", false,
                "message", "Token has expired"
            ));
        }
        
        return ResponseEntity.ok(Map.of(
            "valid", true,
            "message", "Token is valid"
        ));
    }


 
    @PostMapping("/register")
    public ResponseEntity<?> addUser(@RequestBody User newUser) {
        try {
            if (userv.findByEmail(newUser.getEmail()).isPresent()) {
                return ResponseEntity
                        .status(409)
                        .body("Email has already been created!");
            }

            newUser.setRole(Role.USER);
            User saved = userv.createUser(newUser);
            saved.setPassword(null);

            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            return ResponseEntity.status(400).body("Registration failed.");
        }
    }
 
    // Single admin login endpoint that handles both admin and regular users
    @PostMapping("/admin/login")
    public ResponseEntity<?> adminLogin(@RequestBody User loginData, HttpSession session, HttpServletRequest request) {
        try {
            String input = loginData.getEmail();
            String password = loginData.getPassword();
 
            Optional<User> userOpt = userv.findByEmailOrUsername(input);
 
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404).body("User not found");
            }
 
            User user = userOpt.get();
 
            if (!userv.checkPassword(user, password)) {
                return ResponseEntity.status(401).body("Wrong password");
            }

            // IMPORTANT: Also set Spring Security context
        List<GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ROLE_" + user.getRole().toString())
        );
        
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(user.getUsername(), null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);
 
            // Check if user is admin - if not, still return user but with role USER
            // This allows the frontend to redirect based on role
            session.setAttribute("userId", user.getId());
            session.setAttribute("userEmail", user.getEmail());
            session.setAttribute("userRole", user.getRole());
 
            user.setPassword(null);
            return ResponseEntity.ok(user);
 
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Server error: " + e.getMessage());
        }
    }
 
    // LOGOUT - invalidate session
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("Logged out");
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(401).body("Not authenticated");
        }

        Optional<User> userOpt = userv.findById(userId);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }

        User user = userOpt.get();
        user.setPassword(null);

        return ResponseEntity.ok(user);
    }

    // Get all users (admin only)
@GetMapping("/users")
public ResponseEntity<?> getAllUsers(HttpSession session) {
    Integer userId = (Integer) session.getAttribute("userId");
    
    if (userId == null) {
        return ResponseEntity.status(401).body("Not authenticated");
    }
    
    Optional<User> currentUserOpt = userv.findById(userId);
    if (currentUserOpt.isEmpty() || currentUserOpt.get().getRole() != Role.ADMIN) {
        return ResponseEntity.status(403).body("Access denied. Admin only.");
    }
    
    List<User> users = userv.getAllUsers();
    users.forEach(user -> user.setPassword(null));
    return ResponseEntity.ok(users);
}

// Grant admin access to a user (admin only)
@PutMapping("/grant-admin/{userId}")
public ResponseEntity<?> grantAdminAccess(@PathVariable Integer userId, HttpSession session) {
    Integer currentUserId = (Integer) session.getAttribute("userId");
    
    if (currentUserId == null) {
        return ResponseEntity.status(401).body("Not authenticated");
    }
    
    Optional<User> currentUserOpt = userv.findById(currentUserId);
    if (currentUserOpt.isEmpty() || currentUserOpt.get().getRole() != Role.ADMIN) {
        return ResponseEntity.status(403).body("Access denied. Admin only.");
    }
    
    Optional<User> targetUserOpt = userv.findById(userId);
    if (targetUserOpt.isEmpty()) {
        return ResponseEntity.status(404).body("User not found");
    }
    
    User targetUser = targetUserOpt.get();
    targetUser.setRole(Role.ADMIN);
    userv.updateUser(targetUser);
    targetUser.setPassword(null);

    // ADD THIS NOTIFICATION CODE
    String adminUsername = currentUserOpt.get().getUsername();
    notificationService.createNotification(
        "Granted admin access to " + targetUser.getUsername(),
        "GRANT_ADMIN",
        currentUserId,
        adminUsername,
        "User email: " + targetUser.getEmail()
    );

     notificationService.createUserNotification(
        "You have been granted ADMIN access by " + adminUsername,
        "ROLE_GRANTED",
        targetUser.getId(),
        targetUser.getEmail(),
        adminUsername,
        "Your role has been upgraded to ADMIN"
    );
    
    return ResponseEntity.ok(targetUser);
}

// Remove admin access from a user (admin only)
@PutMapping("/remove-admin/{userId}")
public ResponseEntity<?> removeAdminAccess(@PathVariable Integer userId, HttpSession session) {
    Integer currentUserId = (Integer) session.getAttribute("userId");
    
    if (currentUserId == null) {
        return ResponseEntity.status(401).body("Not authenticated");
    }
    
    // Prevent removing own admin access
    if (currentUserId.equals(userId)) {
        return ResponseEntity.badRequest().body("Cannot remove your own admin access");
    }
    
    Optional<User> currentUserOpt = userv.findById(currentUserId);
    if (currentUserOpt.isEmpty() || currentUserOpt.get().getRole() != Role.ADMIN) {
        return ResponseEntity.status(403).body("Access denied. Admin only.");
    }
    
    Optional<User> targetUserOpt = userv.findById(userId);
    if (targetUserOpt.isEmpty()) {
        return ResponseEntity.status(404).body("User not found");
    }
    
    User targetUser = targetUserOpt.get();
    targetUser.setRole(Role.USER);
    userv.updateUser(targetUser);
    targetUser.setPassword(null);

    // ADD THIS NOTIFICATION CODE
    String adminUsername = currentUserOpt.get().getUsername();
    notificationService.createNotification(
        "Removed admin access from " + targetUser.getUsername(),
        "REMOVE_ADMIN",
        currentUserId,
        adminUsername,
        "User email: " + targetUser.getEmail()
    );

    // ADD USER NOTIFICATION for the user who lost admin access
    notificationService.createUserNotification(
        "Your ADMIN access has been removed by " + adminUsername,
        "ROLE_DEMOTED",
        targetUser.getId(),
        targetUser.getEmail(),
        adminUsername,
        "Your role has been changed back to USER"
    );
    
    return ResponseEntity.ok(targetUser);
}

@GetMapping("/debug-console")
public ResponseEntity<?> debugConsole(HttpSession session) {
    Integer userId = (Integer) session.getAttribute("userId");
    String userEmail = (String) session.getAttribute("userEmail");
    String userRole = (String) session.getAttribute("userRole");
    
    Map<String, Object> debug = new HashMap<>();
    debug.put("loggedIn", userId != null);
    debug.put("userId", userId);
    debug.put("userEmail", userEmail);
    debug.put("userRole", userRole);
    debug.put("sessionId", session.getId());
    
    // Also get from database for confirmation
    if (userId != null) {
        Optional<User> userOpt = userv.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            debug.put("dbUsername", user.getUsername());
            debug.put("dbEmail", user.getEmail());
            debug.put("dbRole", user.getRole().toString());
        }
    }
    
    System.out.println("=== DEBUG SESSION ===");
    System.out.println("Logged in: " + (userId != null));
    System.out.println("User ID: " + userId);
    System.out.println("User Email: " + userEmail);
    System.out.println("User Role: " + userRole);
    System.out.println("Session ID: " + session.getId());
    System.out.println("====================");
    
    return ResponseEntity.ok(debug);
}

// Update profile (username only)
@PutMapping("/update-profile")
public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> profileData, HttpSession session) {
    Integer userId = (Integer) session.getAttribute("userId");
    
    if (userId == null) {
        return ResponseEntity.status(401).body("Not authenticated");
    }
    
    Optional<User> userOpt = userv.findById(userId);
    if (userOpt.isEmpty()) {
        return ResponseEntity.status(404).body("User not found");
    }
    
    User user = userOpt.get();
    String newUsername = profileData.get("username");
    
    // Check if username is taken by another user
    Optional<User> existingUser = userv.findByEmailOrUsername(newUsername);
    if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
        return ResponseEntity.badRequest().body(Map.of("error", "Username already taken"));
    }
    
    user.setUsername(newUsername);
    userv.updateUser(user);
    user.setPassword(null);

    notificationService.createUserNotification(
        "You changed your username from to" + newUsername + "'",
        "USERNAME_CHANGE",
        userId,
        user.getEmail(),
        user.getUsername(),
        "Successfully changed username!"
    );
    
    return ResponseEntity.ok(user);
}

}