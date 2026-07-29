package com.inkfront.logisticsApplication.validator.review;

import com.inkfront.logisticsApplication.exception.review.InvalidRatingException;
import org.springframework.stereotype.Component;

@Component
public class ReviewValidator {

    public void validateRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new InvalidRatingException("Rating must be between 1 and 5");
        }
    }

    public void validateComment(String comment) {
        if (comment == null || comment.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment cannot be empty");
        }
        if (comment.length() > 2000) {
            throw new IllegalArgumentException("Comment too long (max 2000 characters)");
        }
    }
}