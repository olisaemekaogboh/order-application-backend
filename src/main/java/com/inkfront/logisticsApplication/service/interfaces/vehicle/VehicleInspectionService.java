package com.inkfront.logisticsApplication.service.interfaces.vehicle;

import com.inkfront.logisticsApplication.dto.request.vehicle.VehicleInspectionRequestDTO;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import com.inkfront.logisticsApplication.dto.response.vehicle.VehicleInspectionDTO;

import java.util.List;

public interface VehicleInspectionService {

    VehicleInspectionDTO scheduleInspection(String vehicleId, VehicleInspectionRequestDTO request, String userId);

    VehicleInspectionDTO updateInspection(String inspectionId, VehicleInspectionRequestDTO request, String userId);

    VehicleInspectionDTO getInspectionById(String inspectionId);

    List<VehicleInspectionDTO> getInspectionsByVehicle(String vehicleId);

    PaginatedResponseDTO<VehicleInspectionDTO> getInspectionHistory(String vehicleId, int page, int size);

    void deleteInspection(String inspectionId, String userId);
}