package com.inkfront.logisticsApplication.listener;

import com.inkfront.logisticsApplication.domain.entity.User;
import com.inkfront.logisticsApplication.domain.entity.Notification;
import com.inkfront.logisticsApplication.domain.enums.NotificationType;
import com.inkfront.logisticsApplication.repository.NotificationRepository;
import com.inkfront.logisticsApplication.service.interfaces.EmailService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventListener {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    @EventListener
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserRegistered(User user) {
        log.info("User registered event received for user: {}", user.getEmail());

        // Send welcome email
        emailService.sendWelcomeEmail(user.getEmail(), user.getFirstName());

        // Create welcome notification
        createNotification(
                user.getId(),
                "Welcome to Logistics Platform",
                "Welcome " + user.getFirstName() + "! Thank you for registering with our platform.",
                NotificationType.SYSTEM,
                null
        );
    }

    @EventListener
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserVerified(User user) {
        log.info("User verified event received for user: {}", user.getEmail());

        createNotification(
                user.getId(),
                "Email Verified",
                "Your email has been successfully verified.",
                NotificationType.SYSTEM,
                null
        );
    }

    @EventListener
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePasswordReset(UserPasswordResetEvent event) {
        User user = event.getUser();
        log.info("Password reset event received for user: {}", user.getEmail());

        createNotification(
                user.getId(),
                "Password Reset",
                "Your password has been successfully reset.",
                NotificationType.SYSTEM,
                null
        );
    }

    @EventListener
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserLogin(UserLoginEvent event) {
        User user = event.getUser();
        log.info("User login event received for user: {}", user.getEmail());

        // Check for unusual login behavior
        if (event.isNewDevice()) {
            createNotification(
                    user.getId(),
                    "New Device Login",
                    "A new device has logged into your account from IP: " + event.getIpAddress(),
                    NotificationType.ALERT,
                    null
            );
        }
    }

    @EventListener
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserAccountLocked(UserAccountLockedEvent event) {
        User user = event.getUser();
        log.warn("User account locked for user: {}", user.getEmail());

        createNotification(
                user.getId(),
                "Account Locked",
                "Your account has been locked due to multiple failed login attempts. Please contact support.",
                NotificationType.ALERT,
                null
        );

        // Send email notification
        emailService.sendEmail(
                user.getEmail(),
                "Account Locked",
                "Your account has been locked due to multiple failed login attempts. Please contact support to unlock your account."
        );
    }

    @EventListener
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserAccountUnlocked(UserAccountUnlockedEvent event) {
        User user = event.getUser();
        log.info("User account unlocked for user: {}", user.getEmail());

        createNotification(
                user.getId(),
                "Account Unlocked",
                "Your account has been unlocked. You can now log in.",
                NotificationType.SYSTEM,
                null
        );
    }

    private void createNotification(String userId, String title, String message,
                                    NotificationType type, String relatedEntityId) {
        Notification notification = new Notification();
        User user = new User();
        user.setId(userId);
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRelatedEntityId(relatedEntityId);
        notification.setCreatedAt(LocalDateTime.now());

        notificationRepository.save(notification);
    }
}
