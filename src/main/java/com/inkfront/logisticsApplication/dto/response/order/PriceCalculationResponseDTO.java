// dto/response/order/PriceCalculationResponseDTO.java
package com.inkfront.logisticsApplication.dto.response.order;

import com.inkfront.logisticsApplication.domain.enums.VehicleType;
import lombok.Data;

import java.util.Map;

@Data
public class PriceCalculationResponseDTO {

    private Double distanceKm;
    private Double weight;
    private Double volume;
    private VehicleType vehicleType;
    private boolean expressDelivery;
    private boolean nightDelivery;

    private Double baseRate;
    private Double basePrice;
    private Double weightSurcharge;
    private Double volumeSurcharge;
    private Double expressSurcharge;
    private Double nightSurcharge;
    private Double totalPrice;
    private Double minimumCharge;

    private String currency;
    private Map<String, Double> breakdown;

    private String vehicleTypeDisplay;
    private String formattedTotalPrice;
    private String estimatedDeliveryTime;
}