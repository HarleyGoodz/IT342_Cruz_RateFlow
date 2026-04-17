package edu.cit.cruz.rateflow.repository;

import edu.cit.cruz.rateflow.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    
    // Admin Notifications
    List<Notification> findAllByOrderByCreatedAtDesc();
    List<Notification> findByAdminIdOrderByCreatedAtDesc(Integer adminId);
    List<Notification> findByNotificationTypeOrderByCreatedAtDesc(String notificationType);
    List<Notification> findByAdminIdAndNotificationTypeOrderByCreatedAtDesc(Integer adminId, String notificationType);
    
    // User Notifications
    List<Notification> findByUserIdOrderByCreatedAtDesc(Integer userId);
    
    @Modifying
    @Transactional
    void deleteByUserIdAndId(Integer userId, Integer id);
    
    @Modifying
    @Transactional
    void deleteByUserId(Integer userId);
}