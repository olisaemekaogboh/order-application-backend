package com.inkfront.logisticsApplication.dto.response.vehicle;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleAssignmentDTO {

    private String id;
    private String vehicleId;
    private String vehicleNumber;
    private String driverId;
    private String driverName;
    private String driverPhone;
    private LocalDateTime assignedAt;
    private LocalDateTime releasedAt;
    private boolean active;
    private String assignmentReason;
    private String releaseReason;
    private String notes;
}