package com.inkfront.logisticsApplication.dto.response.review;

import com.inkfront.logisticsApplication.domain.enums.ModerationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewModerationDTO {

    private String reviewId;
    private String orderNumber;
    private String driverName;
    private Integer rating;
    private String comment;
    private ModerationStatus moderationStatus;
    private String adminRemark;
    private LocalDateTime moderatedAt;
    private String moderatedBy;
}