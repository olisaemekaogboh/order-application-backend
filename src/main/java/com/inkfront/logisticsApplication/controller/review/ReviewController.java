package com.inkfront.logisticsApplication.controller.review;

import com.inkfront.logisticsApplication.domain.constants.SuccessMessages;
import com.inkfront.logisticsApplication.dto.request.review.ReportReviewRequestDTO;
import com.inkfront.logisticsApplication.dto.request.review.ReviewModerationRequestDTO;
import com.inkfront.logisticsApplication.dto.request.review.ReviewRequestDTO;
import com.inkfront.logisticsApplication.dto.response.common.ApiResponseDTO;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import com.inkfront.logisticsApplication.dto.response.review.ReviewResponseDTO;
import com.inkfront.logisticsApplication.dto.response.review.ReviewSummaryDTO;
import com.inkfront.logisticsApplication.security.AuthenticatedUser;
import com.inkfront.logisticsApplication.service.interfaces.review.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Review Management", description = "Review and rating endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @Operation(summary = "Create a new review")
    public ResponseEntity<ApiResponseDTO<ReviewResponseDTO>> createReview(
            Authentication authentication,
            @Valid @RequestBody ReviewRequestDTO request) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        log.info("Create review request by user: {}", user.getId());
        ReviewResponseDTO response = reviewService.createReview(request, user.getId());
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.CREATED_SUCCESSFULLY, response));
    }

    @PutMapping("/{reviewId}")
    @Operation(summary = "Update a review")
    public ResponseEntity<ApiResponseDTO<ReviewResponseDTO>> updateReview(
            @PathVariable String reviewId,
            @Valid @RequestBody ReviewRequestDTO request,
            Authentication authentication) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        log.info("Update review {} by user: {}", reviewId, user.getId());
        ReviewResponseDTO response = reviewService.updateReview(reviewId, request, user.getId());
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.UPDATED_SUCCESSFULLY, response));
    }

    @DeleteMapping("/{reviewId}")
    @Operation(summary = "Delete a review")
    public ResponseEntity<ApiResponseDTO<ReviewResponseDTO>> deleteReview(
            @PathVariable String reviewId,
            Authentication authentication) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        log.info("Delete review {} by user: {}", reviewId, user.getId());
        ReviewResponseDTO response = reviewService.deleteReview(reviewId, user.getId());
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DELETED_SUCCESSFULLY, response));
    }

    @GetMapping("/{reviewId}")
    @Operation(summary = "Get review by ID")
    public ResponseEntity<ApiResponseDTO<ReviewResponseDTO>> getReviewById(@PathVariable String reviewId) {
        log.info("Get review by ID: {}", reviewId);
        ReviewResponseDTO response = reviewService.getReviewById(reviewId);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping("/driver/{driverId}")
    @Operation(summary = "Get reviews for a driver")
    public ResponseEntity<ApiResponseDTO<PaginatedResponseDTO<ReviewSummaryDTO>>> getReviewsByDriver(
            @PathVariable String driverId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        log.info("Get reviews for driver: {}", driverId);
        PaginatedResponseDTO<ReviewSummaryDTO> response = reviewService.getReviewsByDriver(driverId, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping("/customer")
    @Operation(summary = "Get reviews for current customer")
    public ResponseEntity<ApiResponseDTO<PaginatedResponseDTO<ReviewSummaryDTO>>> getReviewsByCustomer(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        log.info("Get reviews for customer: {}", user.getId());
        PaginatedResponseDTO<ReviewSummaryDTO> response = reviewService.getReviewsByCustomer(user.getId(), page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get reviews for an order")
    public ResponseEntity<ApiResponseDTO<PaginatedResponseDTO<ReviewSummaryDTO>>> getReviewsByOrder(
            @PathVariable String orderId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Get reviews for order: {}", orderId);
        PaginatedResponseDTO<ReviewSummaryDTO> response = reviewService.getReviewsByOrder(orderId, page, size);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping
    @Operation(summary = "Get all reviews (admin only)")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<PaginatedResponseDTO<ReviewSummaryDTO>>> getAllReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String moderationStatus,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        log.info("Get all reviews with filters");
        PaginatedResponseDTO<ReviewSummaryDTO> response = reviewService.getAllReviews(page, size, status, moderationStatus, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @PutMapping("/{reviewId}/moderate")
    @Operation(summary = "Moderate a review (admin only)")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<ReviewResponseDTO>> moderateReview(
            @PathVariable String reviewId,
            @Valid @RequestBody ReviewModerationRequestDTO request,
            Authentication authentication) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        log.info("Moderate review {} by user: {}", reviewId, user.getId());
        ReviewResponseDTO response = reviewService.moderateReview(reviewId, request, user.getId());
        return ResponseEntity.ok(ApiResponseDTO.success("Review moderated successfully", response));
    }

    @PostMapping("/{reviewId}/report")
    @Operation(summary = "Report a review")
    public ResponseEntity<ApiResponseDTO<ReviewResponseDTO>> reportReview(
            @PathVariable String reviewId,
            @Valid @RequestBody ReportReviewRequestDTO request,
            Authentication authentication) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        log.info("Report review {} by user: {}", reviewId, user.getId());
        ReviewResponseDTO response = reviewService.reportReview(reviewId, request, user.getId());
        return ResponseEntity.ok(ApiResponseDTO.success("Review reported successfully", response));
    }
}