package com.inkfront.logisticsApplication.service.interfaces.vehicle;

import com.inkfront.logisticsApplication.dto.response.vehicle.VehicleAnalyticsDTO;

public interface FleetService {

    VehicleAnalyticsDTO getFleetAnalytics();

    long countActiveVehicles();

    long countVehiclesByStatus(String status);
}