package edu.cit.cruz.rateflow.service;

import edu.cit.cruz.rateflow.entity.Notification;
import edu.cit.cruz.rateflow.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NotificationService {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    public void createNotification(String message, String type, Integer adminId, String adminUsername, String details) {
        Notification notification = new Notification(message, type, adminId, adminUsername, details);
        notificationRepository.save(notification);
    }
    
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAllByOrderByCreatedAtDesc();
    }
    
    public List<Notification> getNotificationsByAdmin(Integer adminId) {
        return notificationRepository.findByAdminIdOrderByCreatedAtDesc(adminId);
    }
    
    public void deleteNotification(Integer id) {
        notificationRepository.deleteById(id);
    }
    
    public void deleteAllNotifications() {
        notificationRepository.deleteAll();
    }

    public long getUnreadCount() {
    return notificationRepository.count(); // Or add is_read field for proper unread count
}
}