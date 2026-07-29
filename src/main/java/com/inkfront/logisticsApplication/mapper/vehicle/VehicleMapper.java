package com.inkfront.logisticsApplication.mapper.vehicle;

import com.inkfront.logisticsApplication.domain.entity.vehicle.*;
import com.inkfront.logisticsApplication.dto.request.vehicle.*;
import com.inkfront.logisticsApplication.dto.response.vehicle.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VehicleMapper {

    // Vehicle
    Vehicle toEntity(VehicleRequestDTO dto);

    @Mapping(target = "currentDriverId", expression = "java(vehicle.getAssignments().stream().filter(VehicleAssignment::isActive).findFirst().map(a -> a.getDriver().getId()).orElse(null))")
    @Mapping(target = "currentDriverName", expression = "java(vehicle.getAssignments().stream().filter(VehicleAssignment::isActive).findFirst().map(a -> a.getDriver().getName()).orElse(null))")
    VehicleResponseDTO toResponseDTO(Vehicle vehicle);

    VehicleSummaryDTO toSummaryDTO(Vehicle vehicle);

    List<VehicleSummaryDTO> toSummaryDTOList(List<Vehicle> vehicles);

    // Assignment
    @Mapping(target = "vehicleId", source = "vehicle.id")
    @Mapping(target = "vehicleNumber", source = "vehicle.vehicleNumber")
    @Mapping(target = "driverId", source = "driver.id")
    @Mapping(target = "driverName", source = "driver.name")
    @Mapping(target = "driverPhone", source = "driver.phoneNumber")
    VehicleAssignmentDTO toAssignmentDTO(VehicleAssignment assignment);

    // Maintenance
    @Mapping(target = "vehicleId", source = "vehicle.id")
    @Mapping(target = "vehicleNumber", source = "vehicle.vehicleNumber")
    VehicleMaintenanceDTO toMaintenanceDTO(VehicleMaintenance maintenance);

    // Inspection
    @Mapping(target = "vehicleId", source = "vehicle.id")
    @Mapping(target = "vehicleNumber", source = "vehicle.vehicleNumber")
    VehicleInspectionDTO toInspectionDTO(VehicleInspection inspection);
}