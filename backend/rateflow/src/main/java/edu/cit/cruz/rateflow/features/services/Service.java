package edu.cit.cruz.rateflow.features.services;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import edu.cit.cruz.rateflow.features.ratings.Rating;

@Entity
@Table(name = "services")
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "service_id")
    private Integer serviceId;

    private String serviceName;

    private String serviceCategory;

    private String serviceDescription;

    @Lob
    @Column(name = "image")
    private byte[] image;

    private String createdBy;

    // ========== RELATIONSHIPS ==========

    // One Service can have many Ratings
    @OneToMany(mappedBy = "service", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Rating> ratings = new ArrayList<>();

    public Service() {
        super();
    }

    public Service(
            Integer serviceId,
            String serviceName,
            String serviceCategory,
            String serviceDescription,
            byte[] image,
            String createdBy
    ) {
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.serviceCategory = serviceCategory;
        this.serviceDescription = serviceDescription;
        this.image = image;
        this.createdBy = createdBy;
    }

    public Integer getServiceId() {
        return serviceId;
    }

    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceCategory() {
        return serviceCategory;
    }

    public void setServiceCategory(String serviceCategory) {
        this.serviceCategory = serviceCategory;
    }

    public String getServiceDescription() {
        return serviceDescription;
    }

    public void setServiceDescription(String serviceDescription) {
        this.serviceDescription = serviceDescription;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    // ========== RELATIONSHIP GETTERS/SETTERS ==========

    public List<Rating> getRatings() {
        return ratings;
    }

    public void setRatings(List<Rating> ratings) {
        this.ratings = ratings;
    }

    // Helper method to calculate average rating
    public Double getAverageRating() {

        if (ratings == null || ratings.isEmpty()) {
            return 0.0;
        }

        return ratings.stream()
                .mapToInt(Rating::getStarRate)
                .average()
                .orElse(0.0);
    }

    // Helper method to get total ratings count
    public Integer getTotalRatings() {

        return ratings != null ? ratings.size() : 0;
    }
}