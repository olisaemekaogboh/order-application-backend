package com.inkfront.logisticsApplication.service.impl.review;

import com.inkfront.logisticsApplication.domain.entity.Review;
import com.inkfront.logisticsApplication.domain.enums.ModerationStatus;
import com.inkfront.logisticsApplication.domain.enums.ReviewStatus;
import com.inkfront.logisticsApplication.domain.enums.ReviewType;
import com.inkfront.logisticsApplication.dto.response.review.ReviewAnalyticsDTO;
import com.inkfront.logisticsApplication.repository.ReviewRepository;
import com.inkfront.logisticsApplication.service.interfaces.review.ReviewAnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewAnalyticsServiceImpl implements ReviewAnalyticsService {

    private final ReviewRepository reviewRepository;

    @Override
    public ReviewAnalyticsDTO getOverallAnalytics() {
        long total = reviewRepository.count();
        double avg = reviewRepository.findAll().stream()
                .filter(r -> r.getReviewStatus() == ReviewStatus.ACTIVE && r.getModerationStatus() == ModerationStatus.APPROVED)
                .mapToInt(Review::getRating)
                .average().orElse(0.0);

        Map<Integer, Long> dist = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            int star = i;
            Long count = reviewRepository.countByDriverIdAndRating(null, star); // This method is per driver, not global.
            // We'll compute globally by querying all reviews.
            dist.put(star, 0L);
        }
        // We'll implement proper aggregation using repository methods.

        // For simplicity, we'll just fetch all and compute.
        var all = reviewRepository.findAll().stream()
                .filter(r -> r.getReviewStatus() == ReviewStatus.ACTIVE && r.getModerationStatus() == ModerationStatus.APPROVED)
                .collect(Collectors.toList());

        long fiveStar = all.stream().filter(r -> r.getRating() == 5).count();
        long fourStar = all.stream().filter(r -> r.getRating() == 4).count();
        long threeStar = all.stream().filter(r -> r.getRating() == 3).count();
        long twoStar = all.stream().filter(r -> r.getRating() == 2).count();
        long oneStar = all.stream().filter(r -> r.getRating() == 1).count();

        Map<Integer, Long> distribution = new HashMap<>();
        distribution.put(1, oneStar);
        distribution.put(2, twoStar);
        distribution.put(3, threeStar);
        distribution.put(4, fourStar);
        distribution.put(5, fiveStar);

        long pending = reviewRepository.countByModerationStatus(ModerationStatus.PENDING);
        long flagged = reviewRepository.findReportedReviewsPendingModeration().size();

        // Monthly distribution
        Map<String, Long> byMonth = reviewRepository.findReviewsBetweenDates(LocalDateTime.now().minusMonths(12), LocalDateTime.now())
                .stream()
                .filter(r -> r.getReviewStatus() == ReviewStatus.ACTIVE && r.getModerationStatus() == ModerationStatus.APPROVED)
                .collect(Collectors.groupingBy(
                        r -> r.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                        Collectors.counting()
                ));

        // Driver avg
        double driverAvg = reviewRepository.findAll().stream()
                .filter(r -> r.getReviewType() == ReviewType.CUSTOMER_TO_DRIVER && r.getReviewStatus() == ReviewStatus.ACTIVE && r.getModerationStatus() == ModerationStatus.APPROVED)
                .mapToInt(Review::getRating)
                .average().orElse(0.0);

        return ReviewAnalyticsDTO.builder()
                .totalReviews((long) all.size())
                .averageRating(avg)
                .ratingDistribution(distribution)
                .fiveStarCount(fiveStar)
                .fourStarCount(fourStar)
                .threeStarCount(threeStar)
                .twoStarCount(twoStar)
                .oneStarCount(oneStar)
                .pendingModerationCount(pending)
                .flaggedCount(flagged)
                .reviewsByMonth(byMonth)
                .driverAverageRating(driverAvg)
                .build();
    }

    @Override
    public ReviewAnalyticsDTO getAnalyticsForDriver(String driverId) {
        // Similar but filtered by driver
        return new ReviewAnalyticsDTO();
    }

    @Override
    public ReviewAnalyticsDTO getAnalyticsForDateRange(LocalDate startDate, LocalDate endDate) {
        return new ReviewAnalyticsDTO();
    }
}