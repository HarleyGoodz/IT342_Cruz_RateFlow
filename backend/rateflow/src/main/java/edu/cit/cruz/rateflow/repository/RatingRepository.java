package edu.cit.cruz.rateflow.repository;

import edu.cit.cruz.rateflow.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Integer> {
    
    // Check if user has already rated a service
    Optional<Rating> findByUserIdAndServiceId(Integer userId, Integer serviceId);
    
    // Get all ratings for a service (for displaying feedbacks)
    List<Rating> findByServiceIdOrderByDateCreatedDesc(Integer serviceId);
    
    // Get average rating for a service
    @Query("SELECT AVG(r.starRate) FROM Rating r WHERE r.serviceId = :serviceId")
    Double getAverageRatingByServiceId(@Param("serviceId") Integer serviceId);
    
    // Get total number of ratings for a service
    @Query("SELECT COUNT(r) FROM Rating r WHERE r.serviceId = :serviceId")
    Long getTotalRatingsCount(@Param("serviceId") Integer serviceId);

    // Get all ratings by user ID, ordered by most recent first
List<Rating> findByUserIdOrderByDateCreatedDesc(Integer userId);
    
    // Get rating distribution (how many 1-star, 2-star, etc.)
    @Query("SELECT r.starRate, COUNT(r) FROM Rating r WHERE r.serviceId = :serviceId GROUP BY r.starRate")
    List<Object[]> getRatingDistribution(@Param("serviceId") Integer serviceId);
    
    // Check if rating exists
    boolean existsByUserIdAndServiceId(Integer userId, Integer serviceId);
}