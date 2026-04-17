package edu.cit.cruz.rateflow.service;

import edu.cit.cruz.rateflow.entity.Notification;
import edu.cit.cruz.rateflow.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class NotificationService {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    // ============ ADMIN NOTIFICATION METHODS (Keep existing) ============
    
    public void createNotification(String message, String type, Integer adminId, String adminUsername, String details) {
        Notification notification = new Notification(message, type, adminId, adminUsername, details);
        notification.setNotificationType("ADMIN");
        notificationRepository.save(notification);
    }

    public List<Notification> getAllAdminNotifications() {
        return notificationRepository.findByNotificationTypeOrderByCreatedAtDesc("ADMIN");
    }
    
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAllByOrderByCreatedAtDesc();
    }
    
    public List<Notification> getNotificationsByAdmin(Integer adminId) {
        return notificationRepository.findByAdminIdAndNotificationTypeOrderByCreatedAtDesc(adminId, "ADMIN");
    }
    
    public void deleteNotification(Integer id) {
        notificationRepository.deleteById(id);
    }
    
    public void deleteAllAdminNotifications() {
        notificationRepository.deleteAll();
    }

    public long getUnreadCount() {
    return notificationRepository.count();
    }
    
    // ============ USER NOTIFICATION METHODS (Add these) ============
    
    public void createUserNotification(String message, String type, Integer userId, String userEmail, String actorName, String details) {
        Notification notification = new Notification(message, type, userId, userEmail, actorName, details);
        notification.setNotificationType("USER");
        notificationRepository.save(notification);
    }
    
    public List<Notification> getUserNotifications(Integer userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    
    @Transactional
    public void deleteUserNotification(Integer userId, Integer notificationId) {
        notificationRepository.deleteByUserIdAndId(userId, notificationId);
    }
    
    @Transactional
    public void clearAllUserNotifications(Integer userId) {
        notificationRepository.deleteByUserId(userId);
    }
}