package com.inkfront.logisticsApplication.service.impl.vehicle;

import com.inkfront.logisticsApplication.domain.entity.vehicle.Vehicle;
import com.inkfront.logisticsApplication.domain.entity.vehicle.VehicleMaintenance;
import com.inkfront.logisticsApplication.domain.enums.VehicleStatus;
import com.inkfront.logisticsApplication.dto.response.vehicle.VehicleAnalyticsDTO;
import com.inkfront.logisticsApplication.repository.vehicle.VehicleMaintenanceRepository;
import com.inkfront.logisticsApplication.repository.vehicle.VehicleRepository;
import com.inkfront.logisticsApplication.service.interfaces.vehicle.VehicleAnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleAnalyticsServiceImpl implements VehicleAnalyticsService {

    private final VehicleRepository vehicleRepository;
    private final VehicleMaintenanceRepository maintenanceRepository;

    @Override
    public VehicleAnalyticsDTO getAnalytics() {
        log.info("Getting detailed vehicle analytics");

        long total = vehicleRepository.countActiveVehicles();
        long available = vehicleRepository.countByStatus(VehicleStatus.AVAILABLE);
        long assigned = vehicleRepository.countByStatus(VehicleStatus.ASSIGNED);
        long inTransit = vehicleRepository.countByStatus(VehicleStatus.IN_TRANSIT);
        long underMaintenance = vehicleRepository.countByStatus(VehicleStatus.UNDER_MAINTENANCE);
        long outOfService = vehicleRepository.countByStatus(VehicleStatus.OUT_OF_SERVICE);
        long retired = vehicleRepository.countByStatus(VehicleStatus.RETIRED);

        // Due for maintenance (nextMaintenanceDate <= today)
        long dueForMaintenance = vehicleRepository.findVehiclesDueForMaintenance(LocalDate.now()).size();
        long dueForInspection = vehicleRepository.findVehiclesDueForInspection(LocalDate.now()).size();

        // Average mileage
        Double avgMileage = vehicleRepository.findAll().stream()
                .filter(v -> v.getCurrentMileage() != null)
                .mapToDouble(Vehicle::getCurrentMileage)
                .average().orElse(0.0);

        // Total maintenance cost
        Double totalMaintenanceCost = maintenanceRepository.findAll().stream()
                .filter(m -> m.getCost() != null)
                .mapToDouble(VehicleMaintenance::getCost)
                .sum();

        // Average fuel consumption
        Double avgFuelConsumption = vehicleRepository.findAll().stream()
                .filter(v -> v.getFuelConsumption() != null)
                .mapToDouble(Vehicle::getFuelConsumption)
                .average().orElse(0.0);

        // Vehicles by type
        Map<String, Long> byType = vehicleRepository.findAll().stream()
                .collect(Collectors.groupingBy(v -> v.getVehicleType().getDisplayName(), Collectors.counting()));

        // Vehicles by status
        Map<String, Long> byStatus = vehicleRepository.findAll().stream()
                .collect(Collectors.groupingBy(v -> v.getStatus().name(), Collectors.counting()));

        double utilizationRate = total > 0 ? (double) (assigned + inTransit) / total * 100 : 0;

        return VehicleAnalyticsDTO.builder()
                .totalVehicles(total)
                .available(available)
                .assigned(assigned)
                .inTransit(inTransit)
                .underMaintenance(underMaintenance)
                .outOfService(outOfService)
                .retired(retired)
                .dueForMaintenance(dueForMaintenance)
                .dueForInspection(dueForInspection)
                .averageMileage(avgMileage)
                .totalMaintenanceCost(totalMaintenanceCost)
                .averageFuelConsumption(avgFuelConsumption)
                .vehiclesByType(byType)
                .vehiclesByStatus(byStatus)
                .utilizationRate(utilizationRate)
                .build();
    }

    @Override
    public double getUtilizationRate() {
        long total = vehicleRepository.countActiveVehicles();
        long assigned = vehicleRepository.countByStatus(VehicleStatus.ASSIGNED);
        long inTransit = vehicleRepository.countByStatus(VehicleStatus.IN_TRANSIT);
        return total > 0 ? (double) (assigned + inTransit) / total * 100 : 0;
    }

    @Override
    public double getAverageMileage() {
        return vehicleRepository.findAll().stream()
                .filter(v -> v.getCurrentMileage() != null)
                .mapToDouble(Vehicle::getCurrentMileage)
                .average().orElse(0.0);
    }

    @Override
    public double getTotalMaintenanceCost() {
        return maintenanceRepository.findAll().stream()
                .filter(m -> m.getCost() != null)
                .mapToDouble(VehicleMaintenance::getCost)
                .sum();
    }
}