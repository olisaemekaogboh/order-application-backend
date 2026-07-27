package com.inkfront.logisticsApplication.dto.response.pricing;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceCalculationResponseDTO {

    private double basePrice;

    private double weightSurcharge;

    private double volumeSurcharge;

    private double expressSurcharge;

    private double nightSurcharge;

    private double totalPrice;
}