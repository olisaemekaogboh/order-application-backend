package com.inkfront.logisticsApplication.dto.request.review;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportReviewRequestDTO {

    @NotBlank(message = "Report reason is required")
    private String reportReason;
}