package com.inkfront.logisticsApplication.dto.response.review;

import com.inkfront.logisticsApplication.domain.enums.ModerationStatus;
import com.inkfront.logisticsApplication.domain.enums.ReviewStatus;
import com.inkfront.logisticsApplication.domain.enums.ReviewType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponseDTO {

    private String id;
    private String orderId;
    private String orderNumber;
    private String driverId;
    private String driverName;
    private String customerId;
    private String customerName;
    private Integer rating;
    private String title;
    private String comment;
    private ReviewType reviewType;
    private ReviewStatus reviewStatus;
    private ModerationStatus moderationStatus;
    private boolean reported;
    private String reportReason;
    private String adminRemark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime editedAt;
}