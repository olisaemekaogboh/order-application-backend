// dto/response/driver/DriverDTO.java
package com.inkfront.logisticsApplication.dto.response.driver;

import com.inkfront.logisticsApplication.domain.enums.VehicleType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DriverDTO {

    private String id;
    private String name;
    private String email;
    private String phoneNumber;
    private String licenseNumber;
    private VehicleType vehicleType;
    private String vehiclePlateNumber;
    private String vehicleModel;
    private String vehicleTypeDisplay;
    private Double availableBalance;
    private boolean available;
    private Double rating;
    private Integer totalDeliveries;
    private String currentLocation;
    private Double currentLatitude;
    private Double currentLongitude;
    private boolean verified;
    private String bankName;
    private String accountNumber;
    private String accountName;
    private LocalDateTime lastActive;
    private Double totalEarnings;
    private Integer completedOrders;
    private LocalDateTime createdAt;

    private List<DriverEarningDTO> recentEarnings;
    private Integer activeOrders;
    private Double averageRating;
    private String formattedRating;
}