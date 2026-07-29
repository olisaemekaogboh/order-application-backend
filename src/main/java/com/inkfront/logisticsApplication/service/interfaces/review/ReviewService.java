package com.inkfront.logisticsApplication.service.interfaces.review;

import com.inkfront.logisticsApplication.dto.request.review.ReviewRequestDTO;
import com.inkfront.logisticsApplication.dto.request.review.ReviewModerationRequestDTO;
import com.inkfront.logisticsApplication.dto.request.review.ReportReviewRequestDTO;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import com.inkfront.logisticsApplication.dto.response.review.ReviewResponseDTO;
import com.inkfront.logisticsApplication.dto.response.review.ReviewSummaryDTO;

public interface ReviewService {

    ReviewResponseDTO createReview(ReviewRequestDTO request, String userId);

    ReviewResponseDTO updateReview(String reviewId, ReviewRequestDTO request, String userId);

    ReviewResponseDTO deleteReview(String reviewId, String userId);

    ReviewResponseDTO getReviewById(String reviewId);

    PaginatedResponseDTO<ReviewSummaryDTO> getReviewsByDriver(String driverId, int page, int size, String sortBy, String sortDirection);

    PaginatedResponseDTO<ReviewSummaryDTO> getReviewsByCustomer(String customerId, int page, int size, String sortBy, String sortDirection);

    PaginatedResponseDTO<ReviewSummaryDTO> getReviewsByOrder(String orderId, int page, int size);

    PaginatedResponseDTO<ReviewSummaryDTO> getAllReviews(int page, int size, String status, String moderationStatus, String sortBy, String sortDirection);

    ReviewResponseDTO moderateReview(String reviewId, ReviewModerationRequestDTO request, String userId);

    ReviewResponseDTO reportReview(String reviewId, ReportReviewRequestDTO request, String userId);
}