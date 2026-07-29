package com.inkfront.logisticsApplication.events.publisher;

import com.inkfront.logisticsApplication.domain.entity.Review;
import com.inkfront.logisticsApplication.events.review.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public void publishReviewCreated(Review review) {
        eventPublisher.publishEvent(new ReviewCreatedEvent(this, review));
        log.info("Published ReviewCreatedEvent for review: {}", review.getId());
    }

    public void publishReviewUpdated(Review review) {
        eventPublisher.publishEvent(new ReviewUpdatedEvent(this, review));
        log.info("Published ReviewUpdatedEvent for review: {}", review.getId());
    }

    public void publishReviewDeleted(Review review) {
        eventPublisher.publishEvent(new ReviewDeletedEvent(this, review));
        log.info("Published ReviewDeletedEvent for review: {}", review.getId());
    }

    public void publishReviewApproved(Review review) {
        eventPublisher.publishEvent(new ReviewApprovedEvent(this, review));
        log.info("Published ReviewApprovedEvent for review: {}", review.getId());
    }

    public void publishReviewRejected(Review review) {
        eventPublisher.publishEvent(new ReviewRejectedEvent(this, review));
        log.info("Published ReviewRejectedEvent for review: {}", review.getId());
    }

    public void publishReviewReported(Review review) {
        eventPublisher.publishEvent(new ReviewReportedEvent(this, review));
        log.info("Published ReviewReportedEvent for review: {}", review.getId());
    }
}