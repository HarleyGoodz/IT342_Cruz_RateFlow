package edu.cit.cruz.rateflow.controller;

import edu.cit.cruz.rateflow.entity.User;
import edu.cit.cruz.rateflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpSession;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class GoogleAuthController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> payload, HttpSession session) {
        try {
            String idToken = payload.get("idToken");
            
            // Verify Google token and get user info
            String googleUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
            RestTemplate restTemplate = new RestTemplate();
            
            ResponseEntity<Map> response = restTemplate.getForEntity(googleUrl, Map.class);
            Map<String, Object> userInfo = response.getBody();
            
            if (userInfo == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Invalid Google token"));
            }
            
            String email = (String) userInfo.get("email");
            
            // Check if email exists in database
            Optional<User> existingUser = userRepository.findByEmail(email);
            
            if (existingUser.isEmpty()) {
                // Email not registered - return 403 with error message
                return ResponseEntity
                    .status(403)
                    .body(Map.of(
                        "error", "Email not registered",
                        "message", "This email is not registered. Please create an account first.",
                        "email", email
                    ));
            }
            
            // User exists - create session
            User user = existingUser.get();
            session.setAttribute("userId", user.getId());
            session.setAttribute("userEmail", user.getEmail());
            session.setAttribute("userRole", user.getRole());
            session.setAttribute("username", user.getUsername());
            
            // Return user data (no password)
            user.setPassword(null);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "user", user,
                "role", user.getRole().toString()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Google login failed: " + e.getMessage()));
        }
    }
}