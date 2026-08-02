package com.inkfront.logisticsApplication.service.impl.review;

import com.inkfront.logisticsApplication.domain.entity.Driver;
import com.inkfront.logisticsApplication.domain.entity.Order;
import com.inkfront.logisticsApplication.domain.entity.Review;
import com.inkfront.logisticsApplication.domain.entity.User;
import com.inkfront.logisticsApplication.domain.enums.ModerationStatus;
import com.inkfront.logisticsApplication.domain.enums.ReviewStatus;
import com.inkfront.logisticsApplication.dto.request.review.ReportReviewRequestDTO;
import com.inkfront.logisticsApplication.dto.request.review.ReviewModerationRequestDTO;
import com.inkfront.logisticsApplication.dto.request.review.ReviewRequestDTO;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import com.inkfront.logisticsApplication.dto.response.review.ReviewResponseDTO;
import com.inkfront.logisticsApplication.dto.response.review.ReviewSummaryDTO;
import com.inkfront.logisticsApplication.events.publisher.ReviewEventPublisher;
import com.inkfront.logisticsApplication.exception.review.DuplicateReviewException;
import com.inkfront.logisticsApplication.exception.review.ReviewNotFoundException;
import com.inkfront.logisticsApplication.mapper.ReviewMapper;
import com.inkfront.logisticsApplication.repository.DriverRepository;
import com.inkfront.logisticsApplication.repository.OrderRepository;
import com.inkfront.logisticsApplication.repository.ReviewRepository;
import com.inkfront.logisticsApplication.repository.UserRepository;
import com.inkfront.logisticsApplication.service.interfaces.AuditService;
import com.inkfront.logisticsApplication.service.interfaces.review.ReviewNotificationService;
import com.inkfront.logisticsApplication.service.interfaces.review.ReviewService;
import com.inkfront.logisticsApplication.validator.review.ReviewEligibilityValidator;
import com.inkfront.logisticsApplication.validator.review.ReviewValidator;
import com.inkfront.logisticsApplication.validator.review.ReviewModerationValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final DriverRepository driverRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;
    private final ReviewValidator reviewValidator;
    private final ReviewEligibilityValidator eligibilityValidator;
    private final ReviewModerationValidator moderationValidator;
    private final ReviewNotificationService notificationService;
    private final ReviewEventPublisher eventPublisher;
    private final AuditService auditService;

    @Override
    public ReviewResponseDTO createReview(ReviewRequestDTO request, String userId) {
        log.info("Creating review for order: {} by user: {}", request.getOrderId(), userId);

        // Validate order eligibility
        eligibilityValidator.validateOrderEligibleForReview(request.getOrderId(), userId);

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        Driver driver = order.getDriver();
        if (driver == null) {
            throw new IllegalStateException("Order has no driver assigned");
        }

        User customer = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        reviewValidator.validateRating(request.getRating());
        reviewValidator.validateComment(request.getComment());

        Review review = new Review();
        review.setOrder(order);
        review.setDriver(driver);
        review.setCustomer(customer);
        review.setRating(request.getRating());
        review.setTitle(request.getTitle());
        review.setComment(request.getComment());
        review.setReviewType(request.getReviewType());
        review.setReviewStatus(ReviewStatus.ACTIVE);
        review.setModerationStatus(ModerationStatus.PENDING);

        review = reviewRepository.save(review);

        auditService.logAction(userId, "REVIEW_CREATED", "Review", review.getId(),
                "Created review for order " + order.getOrderNumber());

        notificationService.notifyReviewCreated(review);
        eventPublisher.publishReviewCreated(review);

        // Update driver rating stats
        updateDriverRatingStats(driver.getId());

        return reviewMapper.toResponseDTO(review);
    }

    @Override
    public ReviewResponseDTO updateReview(String reviewId, ReviewRequestDTO request, String userId) {
        log.info("Updating review: {} by user: {}", reviewId, userId);

        Review review = findReview(reviewId);

        if (!review.getCustomer().getId().equals(userId)) {
            throw new SecurityException("You are not the owner of this review");
        }

        if (review.getReviewStatus() == ReviewStatus.DELETED) {
            throw new IllegalStateException("Cannot update a deleted review");
        }

        reviewValidator.validateRating(request.getRating());
        reviewValidator.validateComment(request.getComment());

        review.setRating(request.getRating());
        review.setTitle(request.getTitle());
        review.setComment(request.getComment());
        review.setReviewStatus(ReviewStatus.EDITED);
        review.setEditedAt(LocalDateTime.now());

        review = reviewRepository.save(review);

        auditService.logAction(userId, "REVIEW_UPDATED", "Review", review.getId(),
                "Updated review");

        notificationService.notifyReviewUpdated(review);
        eventPublisher.publishReviewUpdated(review);

        // Update driver rating stats
        updateDriverRatingStats(review.getDriver().getId());

        return reviewMapper.toResponseDTO(review);
    }

    @Override
    public ReviewResponseDTO deleteReview(String reviewId, String userId) {
        log.info("Deleting review: {} by user: {}", reviewId, userId);

        Review review = findReview(reviewId);

        if (!review.getCustomer().getId().equals(userId)) {
            throw new SecurityException("You are not the owner of this review");
        }

        review.setReviewStatus(ReviewStatus.DELETED);
        review.setDeleted(true);
        review = reviewRepository.save(review);

        auditService.logAction(userId, "REVIEW_DELETED", "Review", review.getId(),
                "Deleted review");

        notificationService.notifyReviewDeleted(review);
        eventPublisher.publishReviewDeleted(review);

        // Update driver rating stats
        updateDriverRatingStats(review.getDriver().getId());

        return reviewMapper.toResponseDTO(review);
    }

    @Override
    public ReviewResponseDTO getReviewById(String reviewId) {
        Review review = findReview(reviewId);
        return reviewMapper.toResponseDTO(review);
    }

    @Override
    public PaginatedResponseDTO<ReviewSummaryDTO> getReviewsByDriver(String driverId, int page, int size, String sortBy, String sortDirection) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
        Page<Review> pageResult = reviewRepository.findByDriverId(driverId, pageable);
        return toPaginatedResponse(pageResult);
    }

    @Override
    public PaginatedResponseDTO<ReviewSummaryDTO> getReviewsByCustomer(String customerId, int page, int size, String sortBy, String sortDirection) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
        Page<Review> pageResult = reviewRepository.findByCustomerId(customerId, pageable);
        return toPaginatedResponse(pageResult);
    }

    @Override
    public PaginatedResponseDTO<ReviewSummaryDTO> getReviewsByOrder(String orderId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> pageResult = reviewRepository.findByOrderId(orderId, pageable);
        return toPaginatedResponse(pageResult);
    }

    @Override
    public PaginatedResponseDTO<ReviewSummaryDTO> getAllReviews(int page, int size, String status, String moderationStatus, String sortBy, String sortDirection) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
        Page<Review> pageResult;
        if (status != null && moderationStatus != null) {
            // Both filters – we'll use a more complex query; for simplicity, just fetch all and filter later
            pageResult = reviewRepository.findAll(pageable);
        } else if (status != null) {
            pageResult = reviewRepository.findByReviewStatus(ReviewStatus.valueOf(status.toUpperCase()), pageable);
        } else if (moderationStatus != null) {
            pageResult = reviewRepository.findByModerationStatus(ModerationStatus.valueOf(moderationStatus.toUpperCase()), pageable);
        } else {
            pageResult = reviewRepository.findAll(pageable);
        }
        return toPaginatedResponse(pageResult);
    }

    @Override
    public ReviewResponseDTO moderateReview(String reviewId, ReviewModerationRequestDTO request, String userId) {
        log.info("Moderating review: {} by user: {}", reviewId, userId);

        Review review = findReview(reviewId);
        moderationValidator.validateCanModerate(review);
        moderationValidator.validateTransition(review.getModerationStatus(), request.getModerationStatus());

        review.setModerationStatus(request.getModerationStatus());
        review.setAdminRemark(request.getAdminRemark());
        review.setModeratedAt(LocalDateTime.now());
        review.setModeratedBy(userId);

        if (request.getModerationStatus() == ModerationStatus.APPROVED) {
            review.setReviewStatus(ReviewStatus.ACTIVE);
            eventPublisher.publishReviewApproved(review);
            notificationService.notifyReviewApproved(review);
        } else if (request.getModerationStatus() == ModerationStatus.REJECTED) {
            eventPublisher.publishReviewRejected(review);
            notificationService.notifyReviewRejected(review);
        }

        review = reviewRepository.save(review);

        auditService.logAction(userId, "REVIEW_MODERATED", "Review", review.getId(),
                "Moderation status: " + request.getModerationStatus());

        // If approved, update driver rating
        if (request.getModerationStatus() == ModerationStatus.APPROVED) {
            updateDriverRatingStats(review.getDriver().getId());
        }

        return reviewMapper.toResponseDTO(review);
    }

    @Override
    public ReviewResponseDTO reportReview(String reviewId, ReportReviewRequestDTO request, String userId) {
        log.info("Reporting review: {} by user: {}", reviewId, userId);

        Review review = findReview(reviewId);
        review.setReported(true);
        review.setReportReason(request.getReportReason());
        review = reviewRepository.save(review);

        auditService.logAction(userId, "REVIEW_REPORTED", "Review", review.getId(),
                "Report reason: " + request.getReportReason());

        eventPublisher.publishReviewReported(review);
        notificationService.notifyReviewReported(review);

        return reviewMapper.toResponseDTO(review);
    }

    // ------------------- Private Helpers -------------------

    private Review findReview(String reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found: " + reviewId));
    }

    private PaginatedResponseDTO<ReviewSummaryDTO> toPaginatedResponse(Page<Review> page) {
        List<ReviewSummaryDTO> content = page.getContent().stream()
                .map(reviewMapper::toSummaryDTO)
                .collect(Collectors.toList());
        return new PaginatedResponseDTO<>(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }

    private void updateDriverRatingStats(String driverId) {
        try {
            // Compute statistics - handle null values properly
            Double avg = reviewRepository.calculateAverageRatingForDriver(driverId);
            Long totalReviews = reviewRepository.countActiveReviewsForDriver(driverId);
            Long oneStar = reviewRepository.countByDriverIdAndRating(driverId, 1);
            Long twoStar = reviewRepository.countByDriverIdAndRating(driverId, 2);
            Long threeStar = reviewRepository.countByDriverIdAndRating(driverId, 3);
            Long fourStar = reviewRepository.countByDriverIdAndRating(driverId, 4);
            Long fiveStar = reviewRepository.countByDriverIdAndRating(driverId, 5);

            Driver driver = driverRepository.findById(driverId)
                    .orElseThrow(() -> new IllegalArgumentException("Driver not found"));

            // Update driver fields - handle null values with proper defaults
            driver.setRating(avg != null ? avg : 0.0);
            driver.setTotalReviews(totalReviews != null ? totalReviews.intValue() : 0);
            driver.setOneStarCount(oneStar != null ? oneStar.intValue() : 0);
            driver.setTwoStarCount(twoStar != null ? twoStar.intValue() : 0);
            driver.setThreeStarCount(threeStar != null ? threeStar.intValue() : 0);
            driver.setFourStarCount(fourStar != null ? fourStar.intValue() : 0);
            driver.setFiveStarCount(fiveStar != null ? fiveStar.intValue() : 0);

            driverRepository.save(driver);
            log.info("Updated driver rating stats for driver {}: avg={}, total={}, stars: 5={},4={},3={},2={},1={}",
                    driverId, avg, totalReviews, fiveStar, fourStar, threeStar, twoStar, oneStar);
        } catch (Exception e) {
            log.error("Failed to update driver rating stats for driver {}: {}", driverId, e.getMessage());
            // Don't throw the exception - just log it
        }
    }
}