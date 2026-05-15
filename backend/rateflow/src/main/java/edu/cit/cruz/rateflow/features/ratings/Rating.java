package edu.cit.cruz.rateflow.features.ratings;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

import edu.cit.cruz.rateflow.features.services.Service;
import edu.cit.cruz.rateflow.features.authentication.User;

@Entity
@Table(name = "ratings", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"UserID", "ServiceID"}, name = "uk_user_service")
})
public class Rating {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RatingID")
    private Integer ratingId;
    
    @Column(name = "ServiceID", nullable = false)
    private Integer serviceId;
    
    @Column(name = "UserID", nullable = false)
    private Integer userId;
    
    @Column(name = "StarRate", nullable = false)
    private Integer starRate;  // 1-5 stars
    
    @Column(name = "FeedbackText", length = 1000)
    private String feedbackText;  // User's written feedback
    
    @Column(name = "UserName")
    private String userName;  // Store username for display
    
    @Column(name = "DateCreated", nullable = false)
    private LocalDateTime dateCreated;
    
    // ========== ADDED RELATIONSHIPS ==========
    
    // Many Ratings belong to one Service
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ServiceID", referencedColumnName = "service_id", insertable = false, updatable = false)
    @JsonIgnore
    private Service service;
    
    // Many Ratings belong to one User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", referencedColumnName = "userId", insertable = false, updatable = false)
    @JsonIgnore
    private User user;
    
    @PrePersist
    protected void onCreate() {
        dateCreated = LocalDateTime.now();
    }
    
    @PostLoad
    protected void onLoad() {
        // This ensures consistency when loading from database
    }
    
    // Constructors
    public Rating() {}
    
    public Rating(Integer serviceId, Integer userId, Integer starRate, String feedbackText, String userName) {
        this.serviceId = serviceId;
        this.userId = userId;
        this.starRate = starRate;
        this.feedbackText = feedbackText;
        this.userName = userName;
    }
    
    // Getters and Setters
    public Integer getRatingId() {
        return ratingId;
    }
    
    public void setRatingId(Integer ratingId) {
        this.ratingId = ratingId;
    }
    
    public Integer getServiceId() {
        return serviceId;
    }
    
    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
    }
    
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public Integer getStarRate() {
        return starRate;
    }
    
    public void setStarRate(Integer starRate) {
        this.starRate = starRate;
    }
    
    public String getFeedbackText() {
        return feedbackText;
    }
    
    public void setFeedbackText(String feedbackText) {
        this.feedbackText = feedbackText;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public LocalDateTime getDateCreated() {
        return dateCreated;
    }
    
    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }
    
    // ========== ADDED GETTERS AND SETTERS FOR RELATIONSHIPS ==========
    
    public Service getService() {
        return service;
    }
    
    public void setService(Service service) {
        this.service = service;
        if (service != null) {
            this.serviceId = service.getServiceId();
        }
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            this.userId = user.getId();
            this.userName = user.getUsername(); // Auto-sync denormalized field
        }
    }
}