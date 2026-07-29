package com.inkfront.logisticsApplication.dto.response.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewAnalyticsDTO {

    private Long totalReviews;
    private Double averageRating;
    private Map<Integer, Long> ratingDistribution; // star count
    private Long fiveStarCount;
    private Long fourStarCount;
    private Long threeStarCount;
    private Long twoStarCount;
    private Long oneStarCount;
    private Long pendingModerationCount;
    private Long flaggedCount;
    private Map<String, Long> reviewsByMonth; // month -> count
    private Double driverAverageRating;
    private Double customerAverageRating; // optional
}