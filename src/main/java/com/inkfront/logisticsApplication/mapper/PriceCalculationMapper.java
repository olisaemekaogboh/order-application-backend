// mapper/PriceCalculationMapper.java
package com.inkfront.logisticsApplication.mapper;

import com.inkfront.logisticsApplication.domain.enums.VehicleType;
import com.inkfront.logisticsApplication.dto.request.order.PriceCalculationRequestDTO;
import com.inkfront.logisticsApplication.dto.response.order.PriceCalculationResponseDTO;
import org.mapstruct.*;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class PriceCalculationMapper {

    @Mapping(target = "vehicleType", source = "request.vehicleType")
    @Mapping(target = "distanceKm", source = "request.distanceKm")
    @Mapping(target = "weight", source = "request.weight")
    @Mapping(target = "volume", source = "request.volume")
    @Mapping(target = "expressDelivery", source = "request.expressDelivery")
    @Mapping(target = "nightDelivery", source = "request.nightDelivery")
    @Mapping(target = "baseRate", source = "baseRate")
    @Mapping(target = "basePrice", source = "basePrice")
    @Mapping(target = "weightSurcharge", source = "weightSurcharge")
    @Mapping(target = "volumeSurcharge", source = "volumeSurcharge")
    @Mapping(target = "expressSurcharge", source = "expressSurcharge")
    @Mapping(target = "nightSurcharge", source = "nightSurcharge")
    @Mapping(target = "totalPrice", source = "totalPrice")
    @Mapping(target = "minimumCharge", source = "minimumCharge")
    @Mapping(target = "currency", source = "currency")
    @Mapping(target = "vehicleTypeDisplay", expression = "java(request.getVehicleType().getDisplayName())")
    @Mapping(target = "formattedTotalPrice", expression = "java(formatCurrency(totalPrice, currency))")
    @Mapping(target = "estimatedDeliveryTime", expression = "java(calculateEstimatedDeliveryTime(request.getDistanceKm()))")
    @Mapping(target = "breakdown", expression = "java(buildBreakdown(basePrice, weightSurcharge, volumeSurcharge, expressSurcharge, nightSurcharge, totalPrice))")
    public abstract PriceCalculationResponseDTO toDTO(
            PriceCalculationRequestDTO request,
            Double baseRate,
            Double basePrice,
            Double weightSurcharge,
            Double volumeSurcharge,
            Double expressSurcharge,
            Double nightSurcharge,
            Double totalPrice,
            Double minimumCharge,
            String currency
    );

    protected String formatCurrency(Double amount, String currency) {
        if (amount == null) {
            return "0.00";
        }
        if ("NGN".equals(currency)) {
            return "₦" + String.format("%,.2f", amount);
        }
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
        return formatter.format(amount);
    }

    protected String calculateEstimatedDeliveryTime(Double distanceKm) {
        if (distanceKm == null) {
            return "Unknown";
        }
        double estimatedHours = distanceKm / 40.0;
        if (estimatedHours < 1) {
            return "Less than an hour";
        } else if (estimatedHours < 24) {
            return Math.round(estimatedHours) + " hours";
        } else {
            long days = Math.round(estimatedHours / 24);
            return days + " days";
        }
    }

    protected Map<String, Double> buildBreakdown(
            Double basePrice,
            Double weightSurcharge,
            Double volumeSurcharge,
            Double expressSurcharge,
            Double nightSurcharge,
            Double totalPrice
    ) {
        Map<String, Double> breakdown = new HashMap<>();
        breakdown.put("Base Price", basePrice != null ? basePrice : 0.0);
        breakdown.put("Weight Surcharge", weightSurcharge != null ? weightSurcharge : 0.0);
        breakdown.put("Volume Surcharge", volumeSurcharge != null ? volumeSurcharge : 0.0);
        breakdown.put("Express Surcharge", expressSurcharge != null ? expressSurcharge : 0.0);
        breakdown.put("Night Surcharge", nightSurcharge != null ? nightSurcharge : 0.0);
        breakdown.put("Total", totalPrice != null ? totalPrice : 0.0);
        return breakdown;
    }
}