package com.inkfront.logisticsApplication.service.interfaces.vehicle;

import com.inkfront.logisticsApplication.dto.request.vehicle.VehicleMaintenanceRequestDTO;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import com.inkfront.logisticsApplication.dto.response.vehicle.VehicleMaintenanceDTO;

import java.util.List;

public interface VehicleMaintenanceService {

    VehicleMaintenanceDTO scheduleMaintenance(String vehicleId, VehicleMaintenanceRequestDTO request, String userId);

    VehicleMaintenanceDTO updateMaintenance(String maintenanceId, VehicleMaintenanceRequestDTO request, String userId);

    VehicleMaintenanceDTO completeMaintenance(String maintenanceId, String userId);

    VehicleMaintenanceDTO getMaintenanceById(String maintenanceId);

    List<VehicleMaintenanceDTO> getMaintenanceByVehicle(String vehicleId);

    PaginatedResponseDTO<VehicleMaintenanceDTO> getMaintenanceHistory(String vehicleId, int page, int size);

    void deleteMaintenance(String maintenanceId, String userId);
}