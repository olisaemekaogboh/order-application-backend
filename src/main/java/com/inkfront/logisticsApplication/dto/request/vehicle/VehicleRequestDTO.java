package com.inkfront.logisticsApplication.dto.request.vehicle;

import com.inkfront.logisticsApplication.domain.enums.FuelType;
import com.inkfront.logisticsApplication.domain.enums.TransmissionType;
import com.inkfront.logisticsApplication.domain.enums.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleRequestDTO {

    @NotBlank(message = "Vehicle number is required")
    private String vehicleNumber;

    @NotBlank(message = "Registration number is required")
    private String registrationNumber;

    private String plateNumber;
    private String vin;
    private String engineNumber;
    private String chassisNumber;

    private String manufacturer;
    private String brand;
    private String model;
    private Integer year;

    @NotNull(message = "Vehicle type is required")
    private VehicleType vehicleType;

    private FuelType fuelType;
    private TransmissionType transmission;
    private String color;

    @Positive(message = "Capacity must be positive")
    private Double capacityKg;

    @Positive(message = "Volume capacity must be positive")
    private Double capacityVolume;

    @Positive(message = "Max passengers must be positive")
    private Integer maxPassengers;

    private Double fuelConsumption;
    private Double purchasePrice;
    private LocalDate purchaseDate;

    private LocalDate insuranceExpiry;
    private LocalDate roadWorthinessExpiry;
    private LocalDate licenseExpiry;
}