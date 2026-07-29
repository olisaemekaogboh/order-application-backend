package com.inkfront.logisticsApplication.service.interfaces.vehicle;

import com.inkfront.logisticsApplication.dto.request.vehicle.VehicleFilterRequestDTO;
import com.inkfront.logisticsApplication.dto.request.vehicle.VehicleRequestDTO;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import com.inkfront.logisticsApplication.dto.response.vehicle.VehicleResponseDTO;
import com.inkfront.logisticsApplication.dto.response.vehicle.VehicleSummaryDTO;

import java.util.List;

public interface VehicleService {

    VehicleResponseDTO createVehicle(VehicleRequestDTO request, String userId);

    VehicleResponseDTO updateVehicle(String vehicleId, VehicleRequestDTO request, String userId);

    VehicleResponseDTO getVehicleById(String vehicleId);

    VehicleResponseDTO getVehicleByNumber(String vehicleNumber);

    PaginatedResponseDTO<VehicleSummaryDTO> searchVehicles(VehicleFilterRequestDTO filter);

    List<VehicleSummaryDTO> getVehiclesByStatus(String status);

    void deleteVehicle(String vehicleId, String userId);

    VehicleResponseDTO updateVehicleStatus(String vehicleId, String status, String userId);
}