package com.inkfront.logisticsApplication.controller.dispatch;

import com.inkfront.logisticsApplication.domain.constants.SuccessMessages;
import com.inkfront.logisticsApplication.dto.response.common.ApiResponseDTO;
import com.inkfront.logisticsApplication.dto.response.dispatch.DispatchAnalyticsDTO;
import com.inkfront.logisticsApplication.service.interfaces.dispatch.DispatchService;
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
@RequestMapping("/api/dispatch/analytics")
@RequiredArgsConstructor
@Tag(name = "Dispatch Analytics", description = "Dispatch analytics endpoints")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
public class DispatchAnalyticsController {

    private final DispatchService dispatchService;

    @GetMapping
    @Operation(summary = "Get dispatch analytics")
    public ResponseEntity<ApiResponseDTO<DispatchAnalyticsDTO>> getDispatchAnalytics() {
        log.info("Get dispatch analytics");
        DispatchAnalyticsDTO response = dispatchService.getDispatchAnalytics();
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }
}