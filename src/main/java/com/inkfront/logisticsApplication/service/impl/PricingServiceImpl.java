// service/impl/PricingServiceImpl.java
package com.inkfront.logisticsApplication.service.impl;

import com.inkfront.logisticsApplication.domain.entity.PricingConfig;
import com.inkfront.logisticsApplication.domain.enums.VehicleType;
import com.inkfront.logisticsApplication.dto.request.admin.PricingConfigRequestDTO;
import com.inkfront.logisticsApplication.dto.response.admin.PricingConfigDTO;
import com.inkfront.logisticsApplication.exception.BadRequestException;
import com.inkfront.logisticsApplication.exception.ResourceNotFoundException;
import com.inkfront.logisticsApplication.mapper.PricingConfigMapper;
import com.inkfront.logisticsApplication.repository.PricingConfigRepository;
import com.inkfront.logisticsApplication.service.interfaces.PricingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PricingServiceImpl implements PricingService {

    private final PricingConfigRepository pricingConfigRepository;
    private final PricingConfigMapper pricingConfigMapper;

    @Override
    public PricingConfigDTO createPricingConfig(PricingConfigRequestDTO request) {
        log.info("Creating pricing config for vehicle type: {}", request.getVehicleType());

        // Check if active config already exists for this vehicle type
        if (pricingConfigRepository.existsByVehicleTypeAndActiveTrue(request.getVehicleType())) {
            throw new BadRequestException("Active pricing config already exists for vehicle type: " + request.getVehicleType());
        }

        PricingConfig config = pricingConfigMapper.toEntity(request);
        config = pricingConfigRepository.save(config);

        return pricingConfigMapper.toDTO(config);
    }

    @Override
    public PricingConfigDTO updatePricingConfig(String configId, PricingConfigRequestDTO request) {
        log.info("Updating pricing config: {}", configId);

        PricingConfig config = pricingConfigRepository.findById(configId)
                .orElseThrow(() -> new ResourceNotFoundException("Pricing config not found with id: " + configId));

        pricingConfigMapper.updatePricingConfigFromDTO(request, config);
        config = pricingConfigRepository.save(config);

        return pricingConfigMapper.toDTO(config);
    }

    @Override
    public PricingConfigDTO getPricingConfigById(String configId) {
        PricingConfig config = pricingConfigRepository.findById(configId)
                .orElseThrow(() -> new ResourceNotFoundException("Pricing config not found with id: " + configId));
        return pricingConfigMapper.toDTO(config);
    }

    @Override
    public PricingConfigDTO getActivePricingConfig(VehicleType vehicleType) {
        PricingConfig config = pricingConfigRepository.findByVehicleTypeAndActiveTrue(vehicleType)
                .orElseThrow(() -> new ResourceNotFoundException("No active pricing config found for vehicle type: " + vehicleType));
        return pricingConfigMapper.toDTO(config);
    }

    @Override
    public List<PricingConfigDTO> getAllPricingConfigs() {
        return pricingConfigRepository.findAll().stream()
                .map(pricingConfigMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PricingConfigDTO> getActivePricingConfigs() {
        // Use the correct method that returns List<PricingConfig>
        return pricingConfigRepository.findAllActive().stream()
                .map(pricingConfigMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deletePricingConfig(String configId) {
        log.info("Deleting pricing config: {}", configId);

        if (!pricingConfigRepository.existsById(configId)) {
            throw new ResourceNotFoundException("Pricing config not found with id: " + configId);
        }

        pricingConfigRepository.deleteById(configId);
    }

    @Override
    public void activatePricingConfig(String configId) {
        log.info("Activating pricing config: {}", configId);

        PricingConfig config = pricingConfigRepository.findById(configId)
                .orElseThrow(() -> new ResourceNotFoundException("Pricing config not found with id: " + configId));

        // Deactivate all other configs for this vehicle type
        // Use the correct method that returns List<PricingConfig>
        List<PricingConfig> activeConfigs = pricingConfigRepository.findByVehicleType(config.getVehicleType());
        activeConfigs.forEach(c -> {
            if (!c.getId().equals(configId)) {
                c.setActive(false);
                pricingConfigRepository.save(c);
            }
        });

        config.setActive(true);
        config.setEffectiveFrom(LocalDateTime.now());
        pricingConfigRepository.save(config);
    }

    @Override
    public void deactivatePricingConfig(String configId) {
        log.info("Deactivating pricing config: {}", configId);

        PricingConfig config = pricingConfigRepository.findById(configId)
                .orElseThrow(() -> new ResourceNotFoundException("Pricing config not found with id: " + configId));

        config.setActive(false);
        config.setEffectiveTo(LocalDateTime.now());
        pricingConfigRepository.save(config);
    }

    @Override
    public double calculatePrice(VehicleType vehicleType, double distanceKm, double weight, double volume,
                                 boolean express, boolean night) {
        PricingConfig config = getActivePricingConfigEntity(vehicleType);

        double basePrice = calculateBasePrice(vehicleType, distanceKm);
        double weightSurcharge = calculateWeightSurcharge(vehicleType, weight);
        double volumeSurcharge = calculateVolumeSurcharge(vehicleType, volume);
        double expressSurcharge = calculateExpressSurcharge(vehicleType);
        double nightSurcharge = calculateNightSurcharge(vehicleType);

        double total = basePrice + weightSurcharge + volumeSurcharge +
                (express ? expressSurcharge : 0) +
                (night ? nightSurcharge : 0);

        return Math.max(total, config.getMinimumCharge());
    }

    @Override
    public double calculateBasePrice(VehicleType vehicleType, double distanceKm) {
        PricingConfig config = getActivePricingConfigEntity(vehicleType);
        return distanceKm * config.getBaseRatePerKm();
    }

    @Override
    public double calculateWeightSurcharge(VehicleType vehicleType, double weight) {
        PricingConfig config = getActivePricingConfigEntity(vehicleType);
        return weight * config.getWeightSurchargePerKg();
    }

    @Override
    public double calculateVolumeSurcharge(VehicleType vehicleType, double volume) {
        PricingConfig config = getActivePricingConfigEntity(vehicleType);
        return volume * config.getVolumeSurchargePerCubicMeter();
    }

    @Override
    public double calculateExpressSurcharge(VehicleType vehicleType) {
        PricingConfig config = getActivePricingConfigEntity(vehicleType);
        return config.getExpressSurcharge() != null ? config.getExpressSurcharge() : 0.0;
    }

    @Override
    public double calculateNightSurcharge(VehicleType vehicleType) {
        PricingConfig config = getActivePricingConfigEntity(vehicleType);
        return config.getNightSurcharge() != null ? config.getNightSurcharge() : 0.0;
    }

    private PricingConfig getActivePricingConfigEntity(VehicleType vehicleType) {
        return pricingConfigRepository.findByVehicleTypeAndActiveTrue(vehicleType)
                .orElseThrow(() -> new ResourceNotFoundException("No active pricing config found for vehicle type: " + vehicleType));
    }
}