package edu.cit.cruz.rateflow.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
 
@Entity
@Table(name = "services") 
public class Service {
   
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "serviceId")
    private Integer serviceId;

    private String serviceName;
    private String serviceCategory;
    private String serviceDescription;
    
    @Lob
    @Column(name = "image")
    private byte[] image;
    private String createdBy;
 
    public Service() {
        super();
    }
 
    public Service(Integer serviceId, String serviceName, String serviceCategory, String serviceDescription, byte[] image, String createdBy) {
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

    public String getServiceDescription() {
        return serviceDescription;
    }

    public void setServiceDescription(String serviceDescription) {
        this.serviceDescription = serviceDescription;
    }

    public String getServiceCategory() {
        return serviceCategory;
    }
 
    public void setServiceCategory(String serviceCategory) {
        this.serviceCategory = serviceCategory;
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
}