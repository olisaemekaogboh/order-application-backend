package com.inkfront.logisticsApplication.service.impl.review;

import com.inkfront.logisticsApplication.domain.entity.Review;
import com.inkfront.logisticsApplication.service.interfaces.NotificationService;
import com.inkfront.logisticsApplication.service.interfaces.review.ReviewNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewNotificationServiceImpl implements ReviewNotificationService {

    private final NotificationService notificationService;

    @Override
    public void notifyReviewCreated(Review review) {
        String message = String.format("New review received for order %s with rating %d", review.getOrder().getOrderNumber(), review.getRating());
        notificationService.sendSystemNotification(review.getDriver().getId(), "New Review", message);
        notificationService.sendSystemNotification(review.getCustomer().getId(), "Review Submitted", "Your review has been submitted and is pending moderation.");
    }

    @Override
    public void notifyReviewUpdated(Review review) {
        notificationService.sendSystemNotification(review.getDriver().getId(), "Review Updated", "A review has been updated.");
    }

    @Override
    public void notifyReviewApproved(Review review) {
        notificationService.sendSystemNotification(review.getCustomer().getId(), "Review Approved", "Your review has been approved.");
        notificationService.sendSystemNotification(review.getDriver().getId(), "Review Approved", "A review has been approved.");
    }

    @Override
    public void notifyReviewRejected(Review review) {
        notificationService.sendSystemNotification(review.getCustomer().getId(), "Review Rejected", "Your review was rejected. Reason: " + review.getAdminRemark());
    }

    @Override
    public void notifyReviewReported(Review review) {
        notificationService.sendSystemNotification("ADMIN", "Review Reported", "Review for order " + review.getOrder().getOrderNumber() + " has been reported.");
    }

    @Override
    public void notifyReviewDeleted(Review review) {
        notificationService.sendSystemNotification(review.getCustomer().getId(), "Review Deleted", "Your review has been deleted.");
    }
}