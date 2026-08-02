package com.inkfront.logisticsApplication.dto.request.driver;

import com.inkfront.logisticsApplication.domain.enums.VehicleType;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class DriverUpdateRequestDTO {

    private String name;

    @Pattern(regexp = "^(\\+?[0-9]{1,3})?[0-9]{10,15}$", message = "Invalid phone number format")
    private String phoneNumber;

    private String licenseNumber;
    private VehicleType vehicleType;
    private String vehiclePlateNumber;
    private String vehicleModel;
    private Boolean available;
    private String bankName;
    private String accountNumber;
    private String accountName;
    private Double currentLatitude;
    private Double currentLongitude;
}