package edu.cit.cruz.rateflow.controller;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.cit.cruz.rateflow.entity.Role;
import edu.cit.cruz.rateflow.entity.User;
import edu.cit.cruz.rateflow.service.UserService;

import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping(path = "/api/auth")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class UserController {

    @Autowired
    private UserService userv;
 
@PostMapping("/register")
public ResponseEntity<?> addUser(@RequestBody User newUser) {
    try {

        // Check if email already exists
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
 
    // LOGIN - sets HttpSession attribute and returns user (without password)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginData, HttpSession session) {
        try {
            String input = loginData.getEmail(); // can be email or fullname
            String password = loginData.getPassword();
 
            Optional<User> userOpt = userv.findByEmailOrUsername(input);
 
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404).body("User not found");
            }
 
            User user = userOpt.get();
 
            // Plaintext password check (replace with hashed check in production)
            if (!userv.checkPassword(user, password)) {
                return ResponseEntity.status(401).body("Wrong password");
            }
 
            // Set session attributes
            session.setAttribute("userId", user.getId());
            session.setAttribute("userEmail", user.getEmail());
 
            // hide password before returning
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

@PostMapping("/update-username")
public ResponseEntity<?> updateUsername(
        @RequestBody User updatedUser,
        HttpSession session) {

    try {

        Integer userId =
            (Integer) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity
                    .status(401)
                    .body("Not authenticated");
        }

        Optional<User> userOpt =
                userv.findById(userId);

        if (userOpt.isEmpty()) {
            return ResponseEntity
                    .status(404)
                    .body("User not found");
        }

        User user = userOpt.get();

        user.setUsername(
            updatedUser.getUsername()
        );

        User saved =
            userv.createUser(user);

        saved.setPassword(null);

        return ResponseEntity.ok(saved);

    } catch (Exception e) {

        return ResponseEntity
                .status(500)
                .body("Failed to update username");

    }
}



}
