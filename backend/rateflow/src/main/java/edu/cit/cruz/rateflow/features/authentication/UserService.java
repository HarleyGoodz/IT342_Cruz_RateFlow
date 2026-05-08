package edu.cit.cruz.rateflow.features.authentication;

import java.util.List;
import java.util.Optional;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import jakarta.transaction.Transactional;

@Service
public class UserService {
    
    @Autowired
    private UserRepository urepo;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
 
    public UserService() {
        super();
    }
 
 
    // CREATE
    public User createUser(User user) {

    String hashedPassword = passwordEncoder.encode(user.getPassword());
    user.setPassword(hashedPassword);

    // Force role to USER when registering
    user.setRole(Role.USER);

    return urepo.save(user);
}
 
    // FIND BY EMAIL OR FULLNAME (LOGIN)
    public Optional<User> findByEmailOrUsername(String value) {
        Optional<User> user = urepo.findByEmail(value);
 
        if (user.isEmpty()) {
            user = urepo.findByUsername(value);
        }
 
        return user;
    }

    public Optional<User> findByEmail(String email) {
    return urepo.findByEmail(email);
    }
 
    // helper to check raw password vs stored value (plaintext comparison here)
    public boolean checkPassword(User user, String rawPassword) {
        if (user == null || rawPassword == null) return false;
         return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    public Optional<User> findById(Integer id) {
    return urepo.findById(id);
}

public List<User> getAllUsers() {
    List<User> users = urepo.findAll();
    return users != null ? users : List.of();
}

public User updateUser(User user) {
    return urepo.save(user);
    
}

public boolean existsById(Integer id) {
    return urepo.existsById(id);
}

@Transactional
    public boolean updateUsername(Integer userId, String newUsername) {
        Optional<User> userOpt = urepo.findById(userId);
        if (userOpt.isEmpty()) {
            return false;
        }
        
        User user = userOpt.get();

        System.out.println("=== BEFORE UPDATE ===");
        System.out.println("User ID: " + user.getId());
        System.out.println("Username: " + user.getUsername());
        System.out.println("Password exists: " + (user.getPassword() != null ? "YES" : "NO"));
        System.out.println("Password length: " + (user.getPassword() != null ? user.getPassword().length() : 0));

        user.setUsername(newUsername);
        User saved = urepo.save(user); 

        System.out.println("=== AFTER UPDATE ===");
        System.out.println("User ID: " + saved.getId());
        System.out.println("Username: " + saved.getUsername());
        System.out.println("Password exists: " + (saved.getPassword() != null ? "YES" : "NO"));
        System.out.println("Password length: " + (saved.getPassword() != null ? saved.getPassword().length() : 0));
        return true;
    }

}