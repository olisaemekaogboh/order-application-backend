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

    // =========================
    // Dispatch Information
    // =========================
    private String id;
    private DispatchStatus status;
    private Integer priority;
    private Integer retryCount;

    // =========================
    // Order Information
    // =========================
    private String orderId;
    private String orderNumber;
    private String pickupLocation;
    private String deliveryLocation;

    // Customer Information
    private String customerName;
    private String customerPhone;

    // =========================
    // Driver Information
    // =========================
    private String driverId;
    private String driverName;

    // =========================
    // Vehicle Information
    // =========================
    private String vehicleId;
    private String vehicleNumber;

    // =========================
    // Dates
    // =========================
    private LocalDateTime createdAt;
    private LocalDateTime assignedAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime completedAt;
    private LocalDateTime pickupCompletedAt;

    private LocalDateTime deliveryStartedAt;
}