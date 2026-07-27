// dto/request/driver/DriverUpdateRequestDTO.java
package com.inkfront.logisticsApplication.dto.request.driver;

import com.inkfront.logisticsApplication.domain.enums.VehicleType;
import lombok.Data;

@Data
public class DriverUpdateRequestDTO {

    private String name;
    private String phoneNumber;
    private String licenseNumber;
    private VehicleType vehicleType;
    private String vehiclePlateNumber;
    private String vehicleModel;
    private boolean available;
    private String bankName;
    private String accountNumber;
    private String accountName;
    private Double currentLatitude;
    private Double currentLongitude;
}