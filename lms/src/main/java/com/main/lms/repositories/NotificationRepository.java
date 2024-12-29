package com.main.lms.repositories;

import com.main.lms.entities.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdAndIsRead(Long userId, Boolean isRead);

    List<Notification> findByUserId(Long userId);
}