package com.inkfront.logisticsApplication.domain.entity;

import com.inkfront.logisticsApplication.domain.enums.ModerationStatus;
import com.inkfront.logisticsApplication.domain.enums.ReviewStatus;
import com.inkfront.logisticsApplication.domain.enums.ReviewType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@EqualsAndHashCode(
        callSuper = true,
        onlyExplicitlyIncluded = true
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reviews")
public class Review extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @Column(name = "rating", nullable = false)
    private Integer rating; // 1-5

    @Column(name = "title")
    private String title;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_type", nullable = false)
    private ReviewType reviewType = ReviewType.CUSTOMER_TO_DRIVER;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_status", nullable = false)
    private ReviewStatus reviewStatus = ReviewStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "moderation_status", nullable = false)
    private ModerationStatus moderationStatus = ModerationStatus.PENDING;

    @Column(name = "reported")
    private boolean reported = false;

    @Column(name = "report_reason")
    private String reportReason;

    @Column(name = "admin_remark", columnDefinition = "TEXT")
    private String adminRemark;

    @Column(name = "deleted")
    private boolean deleted = false;

    @Column(name = "edited_at")
    private LocalDateTime editedAt;

    @Column(name = "moderated_at")
    private LocalDateTime moderatedAt;

    @Column(name = "moderated_by")
    private String moderatedBy;

    @Version
    @Column(name = "version")
    private Long version;
}