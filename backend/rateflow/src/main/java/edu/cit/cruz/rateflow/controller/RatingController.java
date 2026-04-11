package edu.cit.cruz.rateflow.controller;

import edu.cit.cruz.rateflow.entity.Rating;
import edu.cit.cruz.rateflow.repository.RatingRepository;
import edu.cit.cruz.rateflow.service.RatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {
    
    @Autowired
    private RatingService ratingService;

    @Autowired
    private RatingRepository ratingRepository;
    
    // Submit rating and feedback
    @PostMapping("/submit")
    public ResponseEntity<?> submitRating(@RequestBody Rating rating) {
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
}