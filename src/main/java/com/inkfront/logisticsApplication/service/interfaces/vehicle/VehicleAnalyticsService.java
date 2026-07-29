package com.inkfront.logisticsApplication.service.interfaces.vehicle;

import com.inkfront.logisticsApplication.dto.response.vehicle.VehicleAnalyticsDTO;

public interface VehicleAnalyticsService {

    VehicleAnalyticsDTO getAnalytics();

    double getUtilizationRate();

    double getAverageMileage();

    double getTotalMaintenanceCost();
}