package edu.cit.cruz.rateflow.features.notifications;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user-notifications")
public class UserNotificationController {
    
    @Autowired
    private NotificationService notificationService;
    
    @GetMapping
    public ResponseEntity<?> getUserNotifications(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        
        if (userId == null) {
            return ResponseEntity.status(401).body("Not authenticated");
        }
        
        List<Notification> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(notifications);
    }
    
    
    
    
    @DeleteMapping("/delete/{notificationId}")
    public ResponseEntity<?> deleteNotification(@PathVariable Integer notificationId, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        
        if (userId == null) {
            return ResponseEntity.status(401).body("Not authenticated");
        }
        
        notificationService.deleteUserNotification(userId, notificationId);
        return ResponseEntity.ok(Map.of("success", true));
    }
    
    @DeleteMapping("/clear-all")
    public ResponseEntity<?> clearAllNotifications(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        
        if (userId == null) {
            return ResponseEntity.status(401).body("Not authenticated");
        }
        
        notificationService.clearAllUserNotifications(userId);
        return ResponseEntity.ok(Map.of("success", true));
    }
}