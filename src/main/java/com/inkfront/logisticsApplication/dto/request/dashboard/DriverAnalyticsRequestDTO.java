package com.inkfront.logisticsApplication.dto.request.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverAnalyticsRequestDTO {

    private String driverId;
    private LocalDate startDate;
    private LocalDate endDate;
}