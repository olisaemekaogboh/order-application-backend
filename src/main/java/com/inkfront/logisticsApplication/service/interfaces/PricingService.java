// service/interfaces/PricingService.java
package com.inkfront.logisticsApplication.service.interfaces;

import com.inkfront.logisticsApplication.domain.enums.VehicleType;
import com.inkfront.logisticsApplication.dto.request.admin.PricingConfigRequestDTO;
import com.inkfront.logisticsApplication.dto.response.admin.PricingConfigDTO;

import java.util.List;

public interface PricingService {

    PricingConfigDTO createPricingConfig(PricingConfigRequestDTO request);

    PricingConfigDTO updatePricingConfig(String configId, PricingConfigRequestDTO request);

    PricingConfigDTO getPricingConfigById(String configId);

    PricingConfigDTO getActivePricingConfig(VehicleType vehicleType);

    List<PricingConfigDTO> getAllPricingConfigs();

    List<PricingConfigDTO> getActivePricingConfigs();

    void deletePricingConfig(String configId);

    void activatePricingConfig(String configId);

    void deactivatePricingConfig(String configId);

    double calculatePrice(VehicleType vehicleType, double distanceKm, double weight, double volume, boolean express, boolean night);

    double calculateBasePrice(VehicleType vehicleType, double distanceKm);

    double calculateWeightSurcharge(VehicleType vehicleType, double weight);

    double calculateVolumeSurcharge(VehicleType vehicleType, double volume);

    double calculateExpressSurcharge(VehicleType vehicleType);

    double calculateNightSurcharge(VehicleType vehicleType);
}