package edu.cit.cruz.rateflow.features.ratings;

import edu.cit.cruz.rateflow.features.authentication.User;
import edu.cit.cruz.rateflow.features.authentication.UserService;
import edu.cit.cruz.rateflow.features.notifications.NotificationService;
import edu.cit.cruz.rateflow.features.services.ServiceService;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {
    
    @Autowired
    private RatingService ratingService;

    @Autowired  
    private ServiceService serviceService;  

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;
    
    // Submit rating and feedback
    @PostMapping("/submit")
    public ResponseEntity<?> submitRating(@RequestBody Rating rating, HttpSession session) {
        try {
            // Check if user has already rated this service
            if (ratingService.hasUserRatedService(rating.getUserId(), rating.getServiceId())) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "You have already rated this service",
                    "hasRated", true
                ));
            }
            
            // Validate star rating (1-5)
            if (rating.getStarRate() < 1 || rating.getStarRate() > 5) {
                return ResponseEntity.badRequest().body("Star rate must be between 1 and 5");
            }
            
            Rating savedRating = ratingService.submitRating(rating);

            // ADD USER NOTIFICATION for rating submission
        Integer userId = (Integer) session.getAttribute("userId");
        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isPresent()) {
            notificationService.createUserNotification(
                "You rated a service with " + rating.getStarRate() + " stars",
                "SERVICE_RATING",
                userId,
                userOpt.get().getEmail(),
                userOpt.get().getUsername(),
                "Service ID: " + rating.getServiceId() + ", Feedback: " + 
                (rating.getFeedbackText() != null ? rating.getFeedbackText().substring(0, Math.min(50, rating.getFeedbackText().length())) : "No feedback")
            );
        }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Rating submitted successfully",
                "rating", savedRating
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Error submitting rating: " + e.getMessage()
            ));
        }
    }
    
    // Get rating statistics for a service
    @GetMapping("/service/{serviceId}/stats")
    public ResponseEntity<?> getRatingStats(@PathVariable Integer serviceId) {
        try {
            Map<String, Object> stats = ratingService.getRatingStats(serviceId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Error fetching rating stats"
            ));
        }
    }
    
    // Get all ratings/feedbacks for a service
    @GetMapping("/service/{serviceId}/feedbacks")
    public ResponseEntity<?> getFeedbacksByService(@PathVariable Integer serviceId) {
        try {
            List<Rating> ratings = ratingService.getRatingsByServiceId(serviceId);
            return ResponseEntity.ok(ratings);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Error fetching feedbacks"
            ));
        }
    }
    
    // Check if user has rated a specific service
    @GetMapping("/check/user/{userId}/service/{serviceId}")
    public ResponseEntity<?> checkUserRating(@PathVariable Integer userId, @PathVariable Integer serviceId) {
        try {
            Optional<Rating> rating = ratingService.getUserRatingForService(userId, serviceId);
            if (rating.isPresent()) {
                return ResponseEntity.ok(Map.of(
                    "hasRated", true,
                    "rating", rating.get().getStarRate(),
                    "feedback", rating.get().getFeedbackText()
                ));
            } else {
                return ResponseEntity.ok(Map.of("hasRated", false));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Error checking user rating"
            ));
        }
    }
    
    // Get user's all ratings
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserRatings(@PathVariable Integer userId) {
        try {
            List<Rating> ratings = ratingRepository.findByUserIdOrderByDateCreatedDesc(userId);
            return ResponseEntity.ok(ratings);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error fetching user ratings: " + e.getMessage()));
        }
    }

    // Delete a rating/feedback (admin only)
@DeleteMapping("/delete/{ratingId}")
public ResponseEntity<?> deleteRating(@PathVariable Integer ratingId, HttpSession session) {
    try {
        Optional<Rating> rating = ratingRepository.findById(ratingId);
        if (rating.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Get rating details before deletion
        Rating ratingToDelete = rating.get();
        String userName = ratingToDelete.getUserName();
        Integer serviceId = ratingToDelete.getServiceId();
        Integer starRate = ratingToDelete.getStarRate();
        Integer userId = ratingToDelete.getUserId();
        
        // Get current logged-in user info - FIXED: Handle Role enum properly
        Integer currentUserId = (Integer) session.getAttribute("userId");
        Object userRoleObj = session.getAttribute("userRole");  // This is a Role enum
        String currentUserEmail = (String) session.getAttribute("userEmail");
        
        // Convert Role enum to String
        String currentUserRole = null;
        if (userRoleObj instanceof edu.cit.cruz.rateflow.features.authentication.Role) {
            currentUserRole = ((edu.cit.cruz.rateflow.features.authentication.Role) userRoleObj).toString();
        } else if (userRoleObj instanceof String) {
            currentUserRole = (String) userRoleObj;
        }
        
        // DEBUG: Print values to verify
        System.out.println("currentUserId: " + currentUserId);
        System.out.println("currentUserRole (converted): " + currentUserRole);
        System.out.println("currentUserEmail: " + currentUserEmail);
        System.out.println("rating userId: " + userId);
        
        // Get service name
        String serviceName = "Unknown Service";
        Optional<edu.cit.cruz.rateflow.features.services.Service> serviceOpt = serviceService.getServiceById(serviceId);
        if (serviceOpt.isPresent()) {
            serviceName = serviceOpt.get().getServiceName();
        }
        
        // Check if the person deleting is the rating owner or an admin
        boolean isAdmin = currentUserRole != null && 
                         (currentUserRole.equals("ADMIN") || 
                          currentUserRole.equalsIgnoreCase("admin"));
        boolean isOwner = currentUserId != null && currentUserId.equals(userId);
        
        System.out.println("isAdmin: " + isAdmin);
        System.out.println("isOwner: " + isOwner);
        
        if (!isAdmin && !isOwner) {
            return ResponseEntity.status(403).body(Map.of(
                "error", "You don't have permission to delete this rating",
                "yourRole", currentUserRole,
                "isAdmin", isAdmin,
                "isOwner", isOwner
            ));
        }
        
        ratingRepository.deleteById(ratingId);
        
        // Send notification based on who is deleting
        Optional<User> userOpt = userService.findById(userId);
        
        if (isOwner && !isAdmin) {
            // User deleting their OWN rating
            if (userOpt.isPresent()) {  
                notificationService.createUserNotification(
                    "You have successfully deleted your own rating for service '" + serviceName + "'!",
                    "RATING_DELETED_BY_USER",
                    userId,
                    userOpt.get().getEmail(),
                    userOpt.get().getUsername(),
                    "Your " + starRate + "-star rating and feedback for " + serviceName + " have been removed from the system"
                );
            }
            
            // Also create an admin notification that a user deleted their own rating
            notificationService.createNotification(
                "User " + userName + " deleted their own rating for service '" + serviceName + "'",
                "USER_DELETED_RATING",
                currentUserId,
                currentUserEmail,
                "Service ID: " + serviceId + ", Rating: " + starRate + " stars"
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Your rating has been deleted successfully"
            ));
            
        } else {
            // Admin deleting a user's rating (or admin deleting their own)
            if (userOpt.isPresent()) {
                String notificationMessage;
                if (currentUserId.equals(userId)) {
                    notificationMessage = "You have successfully deleted your own rating for service '" + serviceName + "'!";
                } else {
                    notificationMessage = "Your feedback on service '" + serviceName + "' was deleted by an admin";
                }
                
                notificationService.createUserNotification(
                    notificationMessage,
                    currentUserId.equals(userId) ? "RATING_DELETED_BY_ADMIN_OWN" : "FEEDBACK_DELETED_BY_ADMIN",
                    userId,
                    userOpt.get().getEmail(),
                    currentUserEmail != null ? currentUserEmail : "Admin",
                    "Your " + starRate + "-star rating and feedback for " + serviceName + " have been removed" + 
                    (currentUserId.equals(userId) ? " by you (as admin)" : " by an administrator")
                );
            }
            
            // Admin notification
            notificationService.createNotification(
                (currentUserId.equals(userId) ? "Admin deleted their own rating" : "Deleted feedback from " + userName) + 
                " for service '" + serviceName + "'",
                "DELETE_FEEDBACK_BY_ADMIN",
                currentUserId,
                currentUserEmail,
                "Service ID: " + serviceId + ", Rating: " + starRate + " stars"
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", currentUserId.equals(userId) ? "Your rating has been deleted successfully " : "Feedback deleted successfully"
            ));
        }
        
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(500).body(Map.of(
            "error", "Error deleting feedback: " + e.getMessage()
        ));
    }
}
}
