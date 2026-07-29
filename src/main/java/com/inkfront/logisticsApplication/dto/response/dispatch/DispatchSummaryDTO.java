package com.inkfront.logisticsApplication.dto.response.dispatch;

import com.inkfront.logisticsApplication.domain.enums.DispatchStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DispatchSummaryDTO {

    private String id;
    private String orderNumber;
    private String driverName;
    private String vehicleNumber;
    private DispatchStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}