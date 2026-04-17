package edu.cit.cruz.rateflow.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    private String message;
    
    private String type; // CREATE, UPDATE, DELETE, GRANT_ADMIN, REMOVE_ADMIN, DELETE_FEEDBACK (for admin)
                       // USERNAME_CHANGE, SERVICE_RATING, FEEDBACK_DELETED, ROLE_GRANTED, ROLE_DEMOTED (for user)
    
    // For Admin Notifications
    private Integer adminId;
    private String adminUsername;
    
    // For User Notifications
    private Integer userId;
    private String userEmail;
    private String actorName;
    
    private LocalDateTime createdAt;
    
    @Column(length = 500)
    private String details;
    
    
    private String notificationType; // "ADMIN" or "USER"
    
    public Notification() {}
    
    // Constructor for Admin Notifications
    public Notification(String message, String type, Integer adminId, String adminUsername, String details) {
        this.message = message;
        this.type = type;
        this.adminId = adminId;
        this.adminUsername = adminUsername;
        this.details = details;
        this.createdAt = LocalDateTime.now();
        this.notificationType = "ADMIN";
    }
    
    // Constructor for User Notifications
    public Notification(String message, String type, Integer userId, String userEmail, String actorName, String details) {
        this.message = message;
        this.type = type;
        this.userId = userId;
        this.userEmail = userEmail;
        this.actorName = actorName;
        this.details = details;
        this.createdAt = LocalDateTime.now();
        this.notificationType = "USER";
    }
    
    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public Integer getAdminId() { return adminId; }
    public void setAdminId(Integer adminId) { this.adminId = adminId; }
    
    public String getAdminUsername() { return adminUsername; }
    public void setAdminUsername(String adminUsername) { this.adminUsername = adminUsername; }
    
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    
    public String getActorName() { return actorName; }
    public void setActorName(String actorName) { this.actorName = actorName; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    
    public String getNotificationType() { return notificationType; }
    public void setNotificationType(String notificationType) { this.notificationType = notificationType; }
}