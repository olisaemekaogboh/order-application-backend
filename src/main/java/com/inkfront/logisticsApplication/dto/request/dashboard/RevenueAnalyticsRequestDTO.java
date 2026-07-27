package com.inkfront.logisticsApplication.dto.request.dashboard;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueAnalyticsRequestDTO {

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;
}