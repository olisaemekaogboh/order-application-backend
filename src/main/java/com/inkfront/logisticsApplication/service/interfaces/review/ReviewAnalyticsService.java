package com.inkfront.logisticsApplication.service.interfaces.review;

import com.inkfront.logisticsApplication.dto.response.review.ReviewAnalyticsDTO;

import java.time.LocalDate;

public interface ReviewAnalyticsService {

    ReviewAnalyticsDTO getOverallAnalytics();

    ReviewAnalyticsDTO getAnalyticsForDriver(String driverId);

    ReviewAnalyticsDTO getAnalyticsForDateRange(LocalDate startDate, LocalDate endDate);
}