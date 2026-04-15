package edu.cit.cruz.rateflow.repository;

import edu.cit.cruz.rateflow.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findAllByOrderByCreatedAtDesc();
    List<Notification> findByAdminIdOrderByCreatedAtDesc(Integer adminId);
}