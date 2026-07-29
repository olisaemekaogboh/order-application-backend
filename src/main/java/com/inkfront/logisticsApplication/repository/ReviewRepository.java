package com.inkfront.logisticsApplication.repository;

import com.inkfront.logisticsApplication.domain.entity.Review;
import com.inkfront.logisticsApplication.domain.enums.ModerationStatus;
import com.inkfront.logisticsApplication.domain.enums.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, String> {

    Optional<Review> findByOrderIdAndCustomerId(String orderId, String customerId);

    List<Review> findByDriverId(String driverId);

    Page<Review> findByDriverId(String driverId, Pageable pageable);

    Page<Review> findByCustomerId(String customerId, Pageable pageable);

    Page<Review> findByOrderId(String orderId, Pageable pageable);

    Page<Review> findByReviewStatus(ReviewStatus reviewStatus, Pageable pageable);

    Page<Review> findByModerationStatus(ModerationStatus moderationStatus, Pageable pageable);

    // ===== ADDED METHODS FOR ANALYTICS =====

    @Query("SELECT COUNT(r) FROM Review r WHERE r.moderationStatus = :status")
    long countByModerationStatus(@Param("status") ModerationStatus status);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.reviewStatus = :status")
    long countByReviewStatus(@Param("status") ReviewStatus status);

    @Query("SELECT r FROM Review r WHERE r.driver.id = :driverId AND r.reviewStatus = 'ACTIVE' AND r.moderationStatus = 'APPROVED'")
    List<Review> findActiveApprovedReviewsByDriver(@Param("driverId") String driverId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.driver.id = :driverId AND r.reviewStatus = 'ACTIVE' AND r.moderationStatus = 'APPROVED'")
    Double calculateAverageRatingForDriver(@Param("driverId") String driverId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.driver.id = :driverId AND r.reviewStatus = 'ACTIVE' AND r.moderationStatus = 'APPROVED' AND r.rating = :rating")
    Long countByDriverIdAndRating(@Param("driverId") String driverId, @Param("rating") Integer rating);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.driver.id = :driverId AND r.reviewStatus = 'ACTIVE' AND r.moderationStatus = 'APPROVED'")
    Long countActiveReviewsForDriver(@Param("driverId") String driverId);

    @Query("SELECT r FROM Review r WHERE r.createdAt BETWEEN :start AND :end")
    List<Review> findReviewsBetweenDates(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT r FROM Review r WHERE r.reported = true AND r.moderationStatus = 'PENDING'")
    List<Review> findReportedReviewsPendingModeration();
}