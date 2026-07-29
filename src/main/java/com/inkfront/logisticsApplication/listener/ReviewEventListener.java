package com.inkfront.logisticsApplication.listener;

import com.inkfront.logisticsApplication.events.review.*;
import com.inkfront.logisticsApplication.service.interfaces.review.ReviewNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewEventListener {

    private final ReviewNotificationService notificationService;

    @Async
    @EventListener
    public void handleReviewCreated(ReviewCreatedEvent event) {
        log.info("Handling ReviewCreatedEvent for review: {}", event.getReview().getId());
        notificationService.notifyReviewCreated(event.getReview());
    }

    @Async
    @EventListener
    public void handleReviewUpdated(ReviewUpdatedEvent event) {
        log.info("Handling ReviewUpdatedEvent for review: {}", event.getReview().getId());
        notificationService.notifyReviewUpdated(event.getReview());
    }

    @Async
    @EventListener
    public void handleReviewApproved(ReviewApprovedEvent event) {
        log.info("Handling ReviewApprovedEvent for review: {}", event.getReview().getId());
        notificationService.notifyReviewApproved(event.getReview());
    }

    @Async
    @EventListener
    public void handleReviewRejected(ReviewRejectedEvent event) {
        log.info("Handling ReviewRejectedEvent for review: {}", event.getReview().getId());
        notificationService.notifyReviewRejected(event.getReview());
    }

    @Async
    @EventListener
    public void handleReviewReported(ReviewReportedEvent event) {
        log.info("Handling ReviewReportedEvent for review: {}", event.getReview().getId());
        notificationService.notifyReviewReported(event.getReview());
    }

    @Async
    @EventListener
    public void handleReviewDeleted(ReviewDeletedEvent event) {
        log.info("Handling ReviewDeletedEvent for review: {}", event.getReview().getId());
        notificationService.notifyReviewDeleted(event.getReview());
    }
}