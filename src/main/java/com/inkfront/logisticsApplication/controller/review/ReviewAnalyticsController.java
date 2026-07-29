package com.inkfront.logisticsApplication.controller.review;

import com.inkfront.logisticsApplication.domain.constants.SuccessMessages;
import com.inkfront.logisticsApplication.dto.response.common.ApiResponseDTO;
import com.inkfront.logisticsApplication.dto.response.review.ReviewAnalyticsDTO;
import com.inkfront.logisticsApplication.service.interfaces.review.ReviewAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/reviews/analytics")
@RequiredArgsConstructor
@Tag(name = "Review Analytics", description = "Review analytics endpoints")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class ReviewAnalyticsController {

    private final ReviewAnalyticsService analyticsService;

    @GetMapping
    @Operation(summary = "Get overall review analytics")
    public ResponseEntity<ApiResponseDTO<ReviewAnalyticsDTO>> getAnalytics() {
        log.info("Get review analytics");
        ReviewAnalyticsDTO response = analyticsService.getOverallAnalytics();
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }
}