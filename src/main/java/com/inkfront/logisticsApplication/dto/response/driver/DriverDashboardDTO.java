package com.inkfront.logisticsApplication.dto.response.driver;

import com.inkfront.logisticsApplication.dto.response.dispatch.DispatchSummaryDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DriverDashboardDTO {

    private DriverDTO driver;

    private DispatchSummaryDTO currentDispatch;

    private long activeDispatches;

    private long completedDispatches;

    private double totalEarnings;

    private double unpaidEarnings;

    private int totalDeliveries;

    private double rating;

    private boolean available;

    private String currentLocation;
    private List<DispatchSummaryDTO> recentDispatches;
}