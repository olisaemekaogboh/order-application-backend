package com.inkfront.logisticsApplication.dto.response.vehicle;

import com.inkfront.logisticsApplication.domain.enums.FuelType;
import com.inkfront.logisticsApplication.domain.enums.TransmissionType;
import com.inkfront.logisticsApplication.domain.enums.VehicleStatus;
import com.inkfront.logisticsApplication.domain.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleResponseDTO {

    private String id;
    private String vehicleNumber;
    private String registrationNumber;
    private String plateNumber;
    private String vin;
    private String engineNumber;
    private String chassisNumber;
    private String manufacturer;
    private String brand;
    private String model;
    private Integer year;
    private VehicleType vehicleType;
    private FuelType fuelType;
    private TransmissionType transmission;
    private String color;
    private Double capacityKg;
    private Double capacityVolume;
    private Integer maxPassengers;
    private Double currentMileage;
    private Double fuelConsumption;
    private VehicleStatus status;
    private LocalDate insuranceExpiry;
    private LocalDate roadWorthinessExpiry;
    private LocalDate licenseExpiry;
    private LocalDate purchaseDate;
    private Double purchasePrice;
    private LocalDate lastInspectionDate;
    private LocalDate nextInspectionDate;
    private LocalDate lastMaintenanceDate;
    private LocalDate nextMaintenanceDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String currentDriverId;
    private String currentDriverName;
}