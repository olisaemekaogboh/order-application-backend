package com.inkfront.logisticsApplication.dto.request.review;

import com.inkfront.logisticsApplication.domain.enums.ModerationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewModerationRequestDTO {

    @NotNull(message = "Moderation status is required")
    private ModerationStatus moderationStatus;

    private String adminRemark;
}