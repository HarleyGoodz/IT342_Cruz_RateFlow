package edu.cit.cruz.rateflow.controller;

import edu.cit.cruz.rateflow.entity.Service;
import edu.cit.cruz.rateflow.service.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

    @Autowired
    private ServiceService serviceService;

    // CREATE SERVICE - ADD serviceDescription parameter
    @PostMapping("/create")
    public ResponseEntity<?> createService(
            @RequestParam("serviceName") String serviceName,
            @RequestParam("serviceCategory") String serviceCategory,
            @RequestParam(value = "serviceDescription", required = false) String serviceDescription,
            @RequestParam("createdBy") String createdBy,
            @RequestParam("image") MultipartFile image
    ) throws IOException {

        Service service = new Service();
        service.setServiceName(serviceName);
        service.setServiceCategory(serviceCategory);
        service.setServiceDescription(serviceDescription != null ? serviceDescription : "");
        service.setCreatedBy(createdBy);
        service.setImage(image.getBytes());

        serviceService.createService(service);

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

    // UPDATE SERVICE - ADD serviceDescription parameter
    @PutMapping("/update/{serviceId}")
    public ResponseEntity<?> updateService(
            @PathVariable Integer serviceId,
            @RequestParam String serviceName,
            @RequestParam String serviceCategory,
            @RequestParam(value = "serviceDescription", required = false) String serviceDescription,
            @RequestParam String createdBy,
            @RequestParam(required = false) MultipartFile image
    ) throws IOException {

        Optional<Service> optionalService = serviceService.getServiceById(serviceId);

        if (optionalService.isPresent()) {

            Service service = optionalService.get();

            service.setServiceName(serviceName);
            service.setServiceCategory(serviceCategory);
            service.setServiceDescription(serviceDescription != null ? serviceDescription : "");
            service.setCreatedBy(createdBy);

            if (image != null) {
                service.setImage(image.getBytes());
            }

            serviceService.updateService(service);

            return ResponseEntity.ok("Service updated successfully");
        }

        return ResponseEntity.notFound().build();
    }

    // DELETE SERVICE
    @DeleteMapping("/delete/{serviceId}")
    public ResponseEntity<?> deleteService(@PathVariable Integer serviceId) {

        if (serviceService.exists(serviceId)) {

            serviceService.deleteService(serviceId);

            return ResponseEntity.ok("Service deleted successfully");
        }

        return ResponseEntity.notFound().build();
    }
}