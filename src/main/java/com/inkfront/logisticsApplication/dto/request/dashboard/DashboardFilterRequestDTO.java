package com.inkfront.logisticsApplication.dto.request.dashboard;

import com.inkfront.logisticsApplication.domain.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardFilterRequestDTO {

    private LocalDate startDate;
    private LocalDate endDate;
    private String branchId;
    private String driverId;
    private OrderStatus orderStatus;
}