package com.inkfront.logisticsApplication.service.interfaces.vehicle;

import com.inkfront.logisticsApplication.dto.request.vehicle.VehicleAssignmentRequestDTO;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import com.inkfront.logisticsApplication.dto.response.vehicle.VehicleAssignmentDTO;

import java.util.List;

public interface VehicleAssignmentService {

    VehicleAssignmentDTO assignDriver(String vehicleId, VehicleAssignmentRequestDTO request, String userId);

    VehicleAssignmentDTO releaseDriver(String vehicleId, String reason, String userId);

    VehicleAssignmentDTO getCurrentAssignment(String vehicleId);

    List<VehicleAssignmentDTO> getAssignmentHistory(String vehicleId);

    PaginatedResponseDTO<VehicleAssignmentDTO> getAssignmentsByVehicle(String vehicleId, int page, int size);

    PaginatedResponseDTO<VehicleAssignmentDTO> getAssignmentsByDriver(String driverId, int page, int size);
}