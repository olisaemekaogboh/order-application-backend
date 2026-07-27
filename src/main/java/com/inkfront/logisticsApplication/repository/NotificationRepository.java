package com.inkfront.logisticsApplication.repository;

import com.inkfront.logisticsApplication.domain.entity.Notification;
import com.inkfront.logisticsApplication.domain.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {

    Page<Notification> findByUserId(String userId, Pageable pageable);

    Page<Notification> findByUserIdAndReadFalse(String userId, Pageable pageable);

    List<Notification> findByUserIdAndReadFalse(String userId);

    Page<Notification> findByUserIdAndType(String userId, NotificationType type, Pageable pageable);

    long countByUserIdAndReadFalse(String userId);

    long countByUserId(String userId);

    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.delivered = false")
    List<Notification> findUndeliveredNotifications(@Param("userId") String userId);

    @Query("SELECT n FROM Notification n WHERE n.sentAt <= :date AND n.delivered = false")
    List<Notification> findUndeliveredNotificationsOlderThan(@Param("date") LocalDateTime date);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true, n.readAt = :readAt WHERE n.id = :notificationId")
    void markAsRead(@Param("notificationId") String notificationId, @Param("readAt") LocalDateTime readAt);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true, n.readAt = :readAt WHERE n.user.id = :userId")
    void markAllAsRead(@Param("userId") String userId, @Param("readAt") LocalDateTime readAt);

    @Modifying
    @Query("UPDATE Notification n SET n.delivered = true, n.deliveredAt = :deliveredAt WHERE n.id = :notificationId")
    void markAsDelivered(@Param("notificationId") String notificationId, @Param("deliveredAt") LocalDateTime deliveredAt);
}