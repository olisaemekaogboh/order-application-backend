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

    // ===== BASIC QUERIES =====

    Optional<Review> findById(String id);

    Optional<Review> findByOrderIdAndCustomerId(String orderId, String customerId);

    List<Review> findByDriverId(String driverId);

    Page<Review> findByDriverId(String driverId, Pageable pageable);

    Page<Review> findByCustomerId(String customerId, Pageable pageable);

    Page<Review> findByOrderId(String orderId, Pageable pageable);

    Page<Review> findByReviewStatus(ReviewStatus reviewStatus, Pageable pageable);

    Page<Review> findByModerationStatus(ModerationStatus moderationStatus, Pageable pageable);

    List<Review> findByCustomerId(String customerId);

    List<Review> findByOrderId(String orderId);

    // ===== COUNT QUERIES =====

    @Query("SELECT COUNT(r) FROM Review r WHERE r.moderationStatus = :status")
    long countByModerationStatus(@Param("status") ModerationStatus status);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.reviewStatus = :status")
    long countByReviewStatus(@Param("status") ReviewStatus status);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.driver.id = :driverId")
    long countByDriverId(@Param("driverId") String driverId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.customer.id = :customerId")
    long countByCustomerId(@Param("customerId") String customerId);

    // ===== RATING STATISTICS QUERIES =====

    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.driver.id = :driverId AND r.reviewStatus = 'ACTIVE' AND r.moderationStatus = 'APPROVED'")
    Double calculateAverageRatingForDriver(@Param("driverId") String driverId);

    @Query("SELECT COALESCE(COUNT(r), 0) FROM Review r WHERE r.driver.id = :driverId AND r.reviewStatus = 'ACTIVE' AND r.moderationStatus = 'APPROVED'")
    Long countActiveReviewsForDriver(@Param("driverId") String driverId);

    @Query("SELECT COALESCE(COUNT(r), 0) FROM Review r WHERE r.driver.id = :driverId AND r.reviewStatus = 'ACTIVE' AND r.moderationStatus = 'APPROVED' AND r.rating = :rating")
    Long countByDriverIdAndRating(@Param("driverId") String driverId, @Param("rating") Integer rating);

    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.customer.id = :customerId AND r.reviewStatus = 'ACTIVE' AND r.moderationStatus = 'APPROVED'")
    Double calculateAverageRatingForCustomer(@Param("customerId") String customerId);

    @Query("SELECT COALESCE(COUNT(r), 0) FROM Review r WHERE r.customer.id = :customerId AND r.reviewStatus = 'ACTIVE' AND r.moderationStatus = 'APPROVED'")
    Long countActiveReviewsForCustomer(@Param("customerId") String customerId);

    // ===== OVERALL ANALYTICS QUERIES =====

    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.reviewStatus = 'ACTIVE' AND r.moderationStatus = 'APPROVED'")
    Double calculateOverallAverageRating();

    @Query("SELECT COALESCE(COUNT(r), 0) FROM Review r WHERE r.reviewStatus = 'ACTIVE' AND r.moderationStatus = 'APPROVED'")
    Long countActiveApprovedReviews();

    @Query("SELECT COALESCE(COUNT(r), 0) FROM Review r WHERE r.rating = :rating AND r.reviewStatus = 'ACTIVE' AND r.moderationStatus = 'APPROVED'")
    Long countByRating(@Param("rating") Integer rating);

    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.reviewType = 'CUSTOMER_TO_DRIVER' AND r.reviewStatus = 'ACTIVE' AND r.moderationStatus = 'APPROVED'")
    Double calculateDriverAverageRating();

    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.reviewType = 'DRIVER_TO_CUSTOMER' AND r.reviewStatus = 'ACTIVE' AND r.moderationStatus = 'APPROVED'")
    Double calculateCustomerAverageRating();

    // ===== REPORTED REVIEWS =====

    @Query("SELECT r FROM Review r WHERE r.reported = true AND r.moderationStatus = 'PENDING'")
    List<Review> findReportedReviewsPendingModeration();

    @Query("SELECT COALESCE(COUNT(r), 0) FROM Review r WHERE r.reported = true AND r.moderationStatus = 'PENDING'")
    Long countReportedReviewsPendingModeration();

    @Query("SELECT r FROM Review r WHERE r.reported = true")
    List<Review> findReportedReviews();

    // ===== DATE RANGE QUERIES =====

    @Query("SELECT r FROM Review r WHERE r.createdAt BETWEEN :start AND :end")
    List<Review> findReviewsBetweenDates(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT r FROM Review r WHERE r.createdAt BETWEEN :start AND :end AND r.reviewStatus = 'ACTIVE' AND r.moderationStatus = 'APPROVED'")
    List<Review> findActiveApprovedReviewsBetweenDates(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT r FROM Review r WHERE r.createdAt >= :startDate")
    List<Review> findReviewsFromDate(@Param("startDate") LocalDateTime startDate);

    // ===== MONTHLY STATISTICS =====

    @Query(value = """
        SELECT TO_CHAR(r.created_at, 'YYYY-MM') AS month,
        COUNT(r.id) AS review_count
        FROM reviews r
        WHERE r.review_status = 'ACTIVE' AND r.moderation_status = 'APPROVED'
        GROUP BY TO_CHAR(r.created_at, 'YYYY-MM')
        ORDER BY TO_CHAR(r.created_at, 'YYYY-MM')
    """, nativeQuery = true)
    List<Object[]> countReviewsByMonth();

    @Query(value = """
        SELECT TO_CHAR(r.created_at, 'YYYY-MM') AS month,
        COALESCE(AVG(r.rating), 0) AS avg_rating
        FROM reviews r
        WHERE r.review_status = 'ACTIVE' AND r.moderation_status = 'APPROVED'
        GROUP BY TO_CHAR(r.created_at, 'YYYY-MM')
        ORDER BY TO_CHAR(r.created_at, 'YYYY-MM')
    """, nativeQuery = true)
    List<Object[]> averageRatingByMonth();

    // ===== ACTIVE REVIEWS =====

    @Query("SELECT r FROM Review r WHERE r.driver.id = :driverId AND r.reviewStatus = 'ACTIVE' AND r.moderationStatus = 'APPROVED'")
    List<Review> findActiveApprovedReviewsByDriver(@Param("driverId") String driverId);

    @Query("SELECT r FROM Review r WHERE r.customer.id = :customerId AND r.reviewStatus = 'ACTIVE' AND r.moderationStatus = 'APPROVED'")
    List<Review> findActiveApprovedReviewsByCustomer(@Param("customerId") String customerId);

    // ===== RECENT REVIEWS =====

    @Query("SELECT r FROM Review r ORDER BY r.createdAt DESC")
    List<Review> findRecentReviews(Pageable pageable);

    @Query("SELECT r FROM Review r WHERE r.moderationStatus = 'PENDING' ORDER BY r.createdAt DESC")
    List<Review> findPendingReviews(Pageable pageable);

    // ===== SEARCH QUERIES =====

    @Query("SELECT r FROM Review r WHERE LOWER(r.comment) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(r.title) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Review> searchReviews(@Param("searchTerm") String searchTerm);

    @Query("SELECT r FROM Review r WHERE LOWER(r.comment) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND r.reviewStatus = 'ACTIVE' AND r.moderationStatus = 'APPROVED'")
    List<Review> searchActiveApprovedReviews(@Param("searchTerm") String searchTerm);

    // ===== CUSTOMER REVIEWS =====

    @Query("SELECT r FROM Review r WHERE r.customer.id = :customerId ORDER BY r.createdAt DESC")
    List<Review> findCustomerReviews(@Param("customerId") String customerId);

    @Query("SELECT r FROM Review r WHERE r.driver.id = :driverId ORDER BY r.createdAt DESC")
    List<Review> findDriverReviews(@Param("driverId") String driverId);

    // ===== RATING DISTRIBUTION =====

    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.reviewStatus = 'ACTIVE' AND r.moderationStatus = 'APPROVED' GROUP BY r.rating ORDER BY r.rating")
    List<Object[]> getRatingDistribution();

    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.driver.id = :driverId AND r.reviewStatus = 'ACTIVE' AND r.moderationStatus = 'APPROVED' GROUP BY r.rating ORDER BY r.rating")
    List<Object[]> getRatingDistributionForDriver(@Param("driverId") String driverId);

    // ===== REVIEW STATUS COUNTS =====

    @Query("SELECT r.reviewStatus, COUNT(r) FROM Review r GROUP BY r.reviewStatus")
    List<Object[]> countByReviewStatusGroup();

    @Query("SELECT r.moderationStatus, COUNT(r) FROM Review r GROUP BY r.moderationStatus")
    List<Object[]> countByModerationStatusGroup();
}