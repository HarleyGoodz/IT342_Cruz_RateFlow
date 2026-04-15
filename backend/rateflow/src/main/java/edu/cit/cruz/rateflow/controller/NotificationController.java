package edu.cit.cruz.rateflow.controller;

import edu.cit.cruz.rateflow.entity.Notification;
import edu.cit.cruz.rateflow.entity.Role;
import edu.cit.cruz.rateflow.entity.User;
import edu.cit.cruz.rateflow.service.NotificationService;
import edu.cit.cruz.rateflow.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    public ResponseEntity<?> getNotifications(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        System.out.println("Getting notifications for user: " + userId);
        
        if (userId == null) {
            return ResponseEntity.status(401).body("Not authenticated");
        }
        
        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isEmpty() || userOpt.get().getRole() != Role.ADMIN) {
            return ResponseEntity.status(403).body("Access denied");
        }
        
        List<Notification> notifications = notificationService.getAllNotifications();
        System.out.println("Found " + notifications.size() + " notifications");
        return ResponseEntity.ok(notifications);
    }
    
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable Integer id, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        
        if (userId == null) {
            return ResponseEntity.status(401).body("Not authenticated");
        }
        
        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isEmpty() || userOpt.get().getRole() != Role.ADMIN) {
            return ResponseEntity.status(403).body("Access denied");
        }
        
        notificationService.deleteNotification(id);
        return ResponseEntity.ok(Map.of("success", true));
    }
    
    @DeleteMapping("/clear-all")
    public ResponseEntity<?> clearAllNotifications(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        
        if (userId == null) {
            return ResponseEntity.status(401).body("Not authenticated");
        }
        
        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isEmpty() || userOpt.get().getRole() != Role.ADMIN) {
            return ResponseEntity.status(403).body("Access denied");
        }
        
        notificationService.deleteAllNotifications();
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/unread-count")
public ResponseEntity<?> getUnreadCount(HttpSession session) {
    Integer userId = (Integer) session.getAttribute("userId");
    
    if (userId == null) {
        return ResponseEntity.status(401).body("Not authenticated");
    }
    
    long count = notificationService.getUnreadCount();
    return ResponseEntity.ok(Map.of("count", count));
}
}