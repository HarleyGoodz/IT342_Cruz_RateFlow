package edu.cit.cruz.rateflow.features.authentication;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import com.fasterxml.jackson.annotation.JsonIgnore;

import edu.cit.cruz.rateflow.features.ratings.Rating;
import edu.cit.cruz.rateflow.features.services.Service;
import edu.cit.cruz.rateflow.features.notifications.Notification;
 
@Entity
@Table(name = "users") // matches your MySQL table name
public class User {
   
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userId")
    private Integer id;

    private String username;

    private String resetToken;
    private LocalDateTime resetTokenExpiry;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;
    
    @Enumerated(EnumType.STRING)
    private Role role;
    
    // ========== ADDED RELATIONSHIPS ==========
    
    // One User can have many Ratings
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Rating> ratings = new ArrayList<>();
    
    
    // One User can have many User Notifications
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Notification> userNotifications = new ArrayList<>();
    
    // One User (as Admin) can have many Admin Notifications
    @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Notification> adminNotifications = new ArrayList<>();
 
    public User() {
        super();
    }
 
    public User(Integer id, String username, String email, String password, Role role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // Getters and Setters
    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }
    
    public LocalDateTime getResetTokenExpiry() { return resetTokenExpiry; }
    public void setResetTokenExpiry(LocalDateTime resetTokenExpiry) { this.resetTokenExpiry = resetTokenExpiry; }

    public Integer getId() {
        return id;
    }
 
    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
 
    public String getEmail() {
        return email;
    }
 
    public void setEmail(String email) {
        this.email = email;
    }
 
    public String getPassword() {
        return password;
    }
 
    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }
 
    public void setRole(Role role) {
        this.role = role;
    }
    
    // ========== ADDED GETTERS AND SETTERS FOR RELATIONSHIPS ==========
    
    public List<Rating> getRatings() {
        return ratings;
    }
    
    public void setRatings(List<Rating> ratings) {
        this.ratings = ratings;
    }
    
    
    public List<Notification> getUserNotifications() {
        return userNotifications;
    }
    
    public void setUserNotifications(List<Notification> userNotifications) {
        this.userNotifications = userNotifications;
    }
    
    public List<Notification> getAdminNotifications() {
        return adminNotifications;
    }
    
    public void setAdminNotifications(List<Notification> adminNotifications) {
        this.adminNotifications = adminNotifications;
    }
}