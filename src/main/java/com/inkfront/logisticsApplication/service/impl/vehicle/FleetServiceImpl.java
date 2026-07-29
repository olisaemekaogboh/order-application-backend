package com.inkfront.logisticsApplication.service.impl.vehicle;

import com.inkfront.logisticsApplication.domain.enums.VehicleStatus;
import com.inkfront.logisticsApplication.dto.response.vehicle.VehicleAnalyticsDTO;
import com.inkfront.logisticsApplication.repository.vehicle.VehicleRepository;
import com.inkfront.logisticsApplication.service.interfaces.vehicle.FleetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FleetServiceImpl implements FleetService {

    private final VehicleRepository vehicleRepository;

    @Override
    public VehicleAnalyticsDTO getFleetAnalytics() {
        log.info("Getting fleet analytics");

        long total = vehicleRepository.countActiveVehicles();
        long available = vehicleRepository.countByStatus(VehicleStatus.AVAILABLE);
        long assigned = vehicleRepository.countByStatus(VehicleStatus.ASSIGNED);
        long inTransit = vehicleRepository.countByStatus(VehicleStatus.IN_TRANSIT);
        long underMaintenance = vehicleRepository.countByStatus(VehicleStatus.UNDER_MAINTENANCE);
        long outOfService = vehicleRepository.countByStatus(VehicleStatus.OUT_OF_SERVICE);
        long retired = vehicleRepository.countByStatus(VehicleStatus.RETIRED);

        return VehicleAnalyticsDTO.builder()
                .totalVehicles(total)
                .available(available)
                .assigned(assigned)
                .inTransit(inTransit)
                .underMaintenance(underMaintenance)
                .outOfService(outOfService)
                .retired(retired)
                .build();
    }

    @Override
    public long countActiveVehicles() {
        return vehicleRepository.countActiveVehicles();
    }

    @Override
    public long countVehiclesByStatus(String status) {
        VehicleStatus vehicleStatus = VehicleStatus.valueOf(status.toUpperCase());
        return vehicleRepository.countByStatus(vehicleStatus);
    }
}