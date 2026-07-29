package com.inkfront.logisticsApplication.service.interfaces.review;

import com.inkfront.logisticsApplication.domain.entity.Review;

public interface ReviewNotificationService {

    void notifyReviewCreated(Review review);

    void notifyReviewUpdated(Review review);

    void notifyReviewApproved(Review review);

    void notifyReviewRejected(Review review);

    void notifyReviewReported(Review review);

    void notifyReviewDeleted(Review review);
}