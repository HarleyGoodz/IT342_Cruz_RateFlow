package edu.cit.cruz.rateflow.features.ratings;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class RatingService {
    
    @Autowired
    private RatingRepository ratingRepository;
    
    public Rating submitRating(Rating rating) {
        return ratingRepository.save(rating);
    }
    
    public Optional<Rating> getUserRatingForService(Integer userId, Integer serviceId) {
        return ratingRepository.findByUserIdAndServiceId(userId, serviceId);
    }
    
    public List<Rating> getRatingsByServiceId(Integer serviceId) {
        return ratingRepository.findByServiceIdOrderByDateCreatedDesc(serviceId);
    }
    
    public Map<String, Object> getRatingStats(Integer serviceId) {
        Map<String, Object> stats = new HashMap<>();
        
        Double average = ratingRepository.getAverageRatingByServiceId(serviceId);
        Long total = ratingRepository.getTotalRatingsCount(serviceId);
        
        stats.put("average", average != null ? Math.round(average * 10.0) / 10.0 : 0.0);
        stats.put("total", total != null ? total : 0);
        
        // Get rating distribution (1-5 stars)
        List<Object[]> distribution = ratingRepository.getRatingDistribution(serviceId);
        Map<Integer, Integer> distributionMap = new HashMap<>();
        
        // Initialize with zeros
        for (int i = 1; i <= 5; i++) {
            distributionMap.put(i, 0);
        }
        
        // Fill actual counts
        for (Object[] obj : distribution) {
            Integer rating = ((Number) obj[0]).intValue();
            Long count = (Long) obj[1];
            distributionMap.put(rating, count.intValue());
        }
        
        stats.put("distribution", distributionMap);
        
        return stats;
    }
    
    public boolean hasUserRatedService(Integer userId, Integer serviceId) {
        return ratingRepository.existsByUserIdAndServiceId(userId, serviceId);
    }
}