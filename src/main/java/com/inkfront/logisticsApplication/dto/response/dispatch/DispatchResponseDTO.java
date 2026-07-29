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
public class DispatchResponseDTO {

    private String id;
    private String orderId;
    private String orderNumber;
    private String driverId;
    private String driverName;
    private String vehicleId;
    private String vehicleNumber;
    private DispatchStatus status;
    private LocalDateTime assignedAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime rejectedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private String failureReason;
    private String notes;
    private Integer priority;
    private Integer retryCount;
    private LocalDateTime scheduledTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}