// repository/PricingConfigRepository.java
package com.inkfront.logisticsApplication.repository;

import com.inkfront.logisticsApplication.domain.entity.PricingConfig;
import com.inkfront.logisticsApplication.domain.enums.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PricingConfigRepository extends JpaRepository<PricingConfig, String> {

    // Returns Optional - for single active config
    Optional<PricingConfig> findByVehicleTypeAndActiveTrue(VehicleType vehicleType);

    // Returns List - for all active configs
    List<PricingConfig> findByActiveTrue();

    // Returns List - for all configs of a vehicle type
    List<PricingConfig> findByVehicleType(VehicleType vehicleType);

    // Check if active config exists
    boolean existsByVehicleTypeAndActiveTrue(VehicleType vehicleType);

    // Get all active configs (for the current time)
    @Query("SELECT p FROM PricingConfig p WHERE p.active = true")
    List<PricingConfig> findAllActive();

    // Get active config for specific vehicle type with time validation
    @Query("SELECT p FROM PricingConfig p WHERE p.vehicleType = :vehicleType AND p.active = true AND p.effectiveFrom <= :now AND (p.effectiveTo IS NULL OR p.effectiveTo >= :now)")
    Optional<PricingConfig> findActiveByVehicleType(@Param("vehicleType") VehicleType vehicleType, @Param("now") LocalDateTime now);
}