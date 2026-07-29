package com.inkfront.logisticsApplication.validator.review;

import com.inkfront.logisticsApplication.domain.entity.Review;
import com.inkfront.logisticsApplication.domain.enums.ModerationStatus;
import com.inkfront.logisticsApplication.exception.review.ReviewModerationException;
import org.springframework.stereotype.Component;

@Component
public class ReviewModerationValidator {

    public void validateCanModerate(Review review) {
        if (review.isDeleted()) {
            throw new ReviewModerationException("Cannot moderate a deleted review");
        }
        // Additional business rules
    }

    public void validateTransition(ModerationStatus current, ModerationStatus newStatus) {
        // If already approved, cannot change to pending, etc.
        if (current == ModerationStatus.APPROVED && newStatus != ModerationStatus.APPROVED) {
            throw new ReviewModerationException("Cannot change status of an already approved review");
        }
    }
}