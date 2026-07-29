package com.inkfront.logisticsApplication.dto.response.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewSummaryDTO {

    private String id;
    private String orderNumber;
    private String driverName;
    private String customerName;
    private Integer rating;
    private String title;
    private String commentSnippet; // truncated comment
    private LocalDateTime createdAt;
}