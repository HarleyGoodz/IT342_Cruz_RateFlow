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
    
    private String type; // CREATE, UPDATE, DELETE, GRANT_ADMIN, REMOVE_ADMIN, DELETE_FEEDBACK
    
    private Integer adminId;
    
    private String adminUsername;
    
    private LocalDateTime createdAt;
    
    @Column(length = 500)
    private String details;
    
    public Notification() {}
    
    public Notification(String message, String type, Integer adminId, String adminUsername, String details) {
        this.message = message;
        this.type = type;
        this.adminId = adminId;
        this.adminUsername = adminUsername;
        this.details = details;
        this.createdAt = LocalDateTime.now();
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
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}