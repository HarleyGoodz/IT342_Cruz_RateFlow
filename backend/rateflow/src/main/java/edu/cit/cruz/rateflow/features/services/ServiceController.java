package edu.cit.cruz.rateflow.features.services;

import edu.cit.cruz.rateflow.features.authentication.User;
import edu.cit.cruz.rateflow.features.authentication.UserRepository;
import edu.cit.cruz.rateflow.features.notifications.NotificationService;
import edu.cit.cruz.rateflow.features.ratings.Rating;
import edu.cit.cruz.rateflow.features.ratings.RatingRepository;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/api/services")

public class ServiceController {

    @Autowired
    private ServiceService serviceService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private UserRepository userRepository; 

    // CREATE SERVICE
    @PostMapping("/create")
    public ResponseEntity<?> createService(
            @RequestParam("serviceName") String serviceName,
            @RequestParam("serviceCategory") String serviceCategory,
            @RequestParam(value = "serviceDescription", required = false) String serviceDescription,
            @RequestParam("createdBy") String createdBy,
            @RequestParam("image") MultipartFile image,
            HttpSession session
    ) throws IOException {

        Service service = new Service();
        service.setServiceName(serviceName);
        service.setServiceCategory(serviceCategory);
        service.setServiceDescription(serviceDescription != null ? serviceDescription : "");
        service.setCreatedBy(createdBy);
        service.setImage(image.getBytes());

        serviceService.createService(service);

        Integer adminId = (Integer) session.getAttribute("userId");
        String adminUsername = createdBy;

        notificationService.createNotification(
            "Created new service: " + serviceName,
            "CREATE",
            adminId,
            adminUsername,
            "Category: " + serviceCategory
        );

        return ResponseEntity.ok("Service created successfully");
    }

    // GET ALL SERVICES
    @GetMapping
    public List<Service> getAllServices() {
        return serviceService.getAllServices();
    }

    // GET SERVICE BY ID
    @GetMapping("/{serviceId}")
    public ResponseEntity<Service> getServiceById(@PathVariable Integer serviceId) {

        Optional<Service> service = serviceService.getServiceById(serviceId);

        if (service.isPresent()) {
            return ResponseEntity.ok(service.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // GET SERVICE IMAGE
    @GetMapping("/{serviceId}/image")
    public ResponseEntity<byte[]> getServiceImage(@PathVariable Integer serviceId) {

        Optional<Service> service = serviceService.getServiceById(serviceId);

        if (service.isPresent()) {

            return ResponseEntity
                    .ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(service.get().getImage());
        }

        return ResponseEntity.notFound().build();
    }

    // UPDATE SERVICE
    @PutMapping("/update/{serviceId}")
    public ResponseEntity<?> updateService(
            @PathVariable Integer serviceId,
            @RequestParam String serviceName,
            @RequestParam String serviceCategory,
            @RequestParam(value = "serviceDescription", required = false) String serviceDescription,
            @RequestParam String createdBy,
            @RequestParam(required = false) MultipartFile image,
            HttpSession session
    ) throws IOException {

        Optional<Service> optionalService = serviceService.getServiceById(serviceId);

        if (optionalService.isPresent()) {

            Service service = optionalService.get();
            
            // Store old values to track changes
            String oldServiceName = service.getServiceName();
            String oldCategory = service.getServiceCategory();
            String oldDescription = service.getServiceDescription();

            service.setServiceName(serviceName);
            service.setServiceCategory(serviceCategory);
            service.setServiceDescription(serviceDescription != null ? serviceDescription : "");
            service.setCreatedBy(createdBy);

            if (image != null) {
                service.setImage(image.getBytes());
            }

            serviceService.updateService(service);

            // ADMIN NOTIFICATION CODE (existing)
            Integer adminId = (Integer) session.getAttribute("userId");
            String adminUsername = createdBy;
            notificationService.createNotification(
                "Updated service: " + serviceName,
                "UPDATE",
                adminId,
                adminUsername,
                "Service ID: " + serviceId + ", Category: " + serviceCategory
            );
            
            // NEW: USER NOTIFICATIONS FOR SERVICE UPDATE
            // Get all users who rated this service
            List<Rating> ratings = ratingRepository.findByServiceIdOrderByDateCreatedDesc(serviceId);
            
            // Track unique users to avoid duplicate notifications
            Set<Integer> uniqueUserIds = new HashSet<>();
            
            // Build changes summary
            StringBuilder changes = new StringBuilder();
            if (!oldServiceName.equals(serviceName)) {
                changes.append("Name changed from '").append(oldServiceName).append("' to '").append(serviceName).append("'. ");
            }
            if (!oldCategory.equals(serviceCategory)) {
                changes.append("Category changed from '").append(oldCategory).append("' to '").append(serviceCategory).append("'. ");
            }
            if (oldDescription != null && serviceDescription != null && !oldDescription.equals(serviceDescription)) {
                changes.append("Description was updated. ");
            }
            
            String changesMessage = changes.length() > 0 ? changes.toString() : "Service details were updated.";
            
            // Notify each unique user who rated this service
            for (Rating rating : ratings) {
                Integer userId = rating.getUserId();
                if (!uniqueUserIds.contains(userId)) {
                    uniqueUserIds.add(userId);
                    
                    Optional<User> userOpt = userRepository.findById(userId);
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        
                        notificationService.createUserNotification(
                            "Service '" + serviceName + "' has been updated by an admin",
                            "SERVICE_UPDATED",
                            userId,
                            user.getEmail(),
                            adminUsername != null ? adminUsername : "Admin",
                            changesMessage
                        );
                    }
                }
            }

            return ResponseEntity.ok("Service updated successfully");
        }

        return ResponseEntity.notFound().build();
    }

    // DELETE SERVICE - Updated to delete ratings first
    @DeleteMapping("/delete/{serviceId}")
    public ResponseEntity<?> deleteService(@PathVariable Integer serviceId, HttpSession session) {

        if (serviceService.exists(serviceId)) {

            // Get service details before deletion for notification
            Optional<Service> serviceOpt = serviceService.getServiceById(serviceId);
            String serviceName = serviceOpt.map(Service::getServiceName).orElse("Unknown");
            String createdBy = serviceOpt.map(Service::getCreatedBy).orElse("Unknown");

            // Get all ratings to notify users before deletion
            List<Rating> ratings = ratingRepository.findByServiceIdOrderByDateCreatedDesc(serviceId);
            Set<Integer> uniqueUserIds = new HashSet<>();
            
            // Get admin info for notifications
            Integer adminId = (Integer) session.getAttribute("userId");
            String adminUsername = (String) session.getAttribute("userEmail");
            
            // Notify users before deleting the service
            for (Rating rating : ratings) {
                Integer userId = rating.getUserId();
                if (!uniqueUserIds.contains(userId)) {
                    uniqueUserIds.add(userId);
                    
                    Optional<User> userOpt = userRepository.findById(userId);
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        
                        notificationService.createUserNotification(
                            "Service '" + serviceName + "' has been deleted by an admin",
                            "SERVICE_DELETED",
                            userId,
                            user.getEmail(),
                            adminUsername != null ? adminUsername : "Admin",
                            "Your rating for this service has been removed"
                        );
                    }
                }
            }
            
            // First, delete all ratings associated with this service
            if (!ratings.isEmpty()) {
                ratingRepository.deleteAll(ratings);
            }
            
            // Then delete the service
            serviceService.deleteService(serviceId);

            // ADMIN NOTIFICATION CODE (existing)
            notificationService.createNotification(
                "Deleted service: " + serviceName,
                "DELETE",
                adminId,
                adminUsername,
                "Service ID: " + serviceId + ", Created by: " + createdBy
            );

            return ResponseEntity.ok("Service and its associated ratings deleted successfully");
        }

        return ResponseEntity.notFound().build();
    }
}