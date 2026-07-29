package com.inkfront.logisticsApplication.dto.response.dispatch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DispatchAssignmentResult {
    private String driverId;
    private String driverName;
    private String vehicleId;
    private String vehicleNumber;
    private boolean success;
    private String message;
}