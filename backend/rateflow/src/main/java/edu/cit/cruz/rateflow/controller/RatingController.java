package edu.cit.cruz.rateflow.controller;

import edu.cit.cruz.rateflow.entity.Rating;
import edu.cit.cruz.rateflow.entity.User;
import edu.cit.cruz.rateflow.repository.RatingRepository;
import edu.cit.cruz.rateflow.service.NotificationService;
import edu.cit.cruz.rateflow.service.RatingService;
import edu.cit.cruz.rateflow.service.ServiceService;
import edu.cit.cruz.rateflow.service.UserService;
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
            
            // Get service name
            String serviceName = "Unknown Service";
            Optional<edu.cit.cruz.rateflow.entity.Service> serviceOpt = serviceService.getServiceById(serviceId);
            if (serviceOpt.isPresent()) {
                serviceName = serviceOpt.get().getServiceName();
            }
            
            ratingRepository.deleteById(ratingId);

            // User notification for the user whose feedback was deleted
            Optional<User> userOpt = userService.findById(userId);
            if (userOpt.isPresent()) {
                notificationService.createUserNotification(
                    "Your feedback on service '" + serviceName + "' was deleted by an admin",
                    "FEEDBACK_DELETED",
                    userId,
                    userOpt.get().getEmail(),
                    "Admin",
                    "Your " + starRate + "-star rating and feedback have been removed"
                );
            }

            // Admin notification
            Integer adminId = (Integer) session.getAttribute("userId");
            String adminUsername = (String) session.getAttribute("userEmail");
            notificationService.createNotification(
                "Deleted feedback from " + userName + " for service '" + serviceName + "'",
                "DELETE_FEEDBACK",
                adminId,
                adminUsername,
                "Service ID: " + serviceId + ", Rating: " + starRate + " stars"
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Feedback deleted successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Error deleting feedback: " + e.getMessage()
            ));
        }
    }
}
