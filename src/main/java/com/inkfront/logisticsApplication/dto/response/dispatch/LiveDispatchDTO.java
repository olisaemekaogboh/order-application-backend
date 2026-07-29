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
public class LiveDispatchDTO {
    private String dispatchId;
    private String orderNumber;
    private DispatchStatus status;
    private String driverName;
    private String driverPhone;
    private String vehicleNumber;
    private Double driverLatitude;
    private Double driverLongitude;
    private LocalDateTime lastUpdate;
    private LocalDateTime estimatedArrival;
    private String etaText;
}