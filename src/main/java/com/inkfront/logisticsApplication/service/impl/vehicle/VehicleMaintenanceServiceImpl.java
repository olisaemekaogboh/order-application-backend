package com.inkfront.logisticsApplication.service.impl.vehicle;

import com.inkfront.logisticsApplication.domain.entity.vehicle.Vehicle;
import com.inkfront.logisticsApplication.domain.entity.vehicle.VehicleMaintenance;
import com.inkfront.logisticsApplication.domain.enums.MaintenanceStatus;
import com.inkfront.logisticsApplication.dto.request.vehicle.VehicleMaintenanceRequestDTO;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import com.inkfront.logisticsApplication.dto.response.vehicle.VehicleMaintenanceDTO;
import com.inkfront.logisticsApplication.exception.vehicle.MaintenanceException;
import com.inkfront.logisticsApplication.exception.vehicle.VehicleNotFoundException;
import com.inkfront.logisticsApplication.mapper.vehicle.VehicleMapper;
import com.inkfront.logisticsApplication.repository.vehicle.VehicleMaintenanceRepository;
import com.inkfront.logisticsApplication.repository.vehicle.VehicleRepository;
import com.inkfront.logisticsApplication.service.interfaces.AuditService;
import com.inkfront.logisticsApplication.service.interfaces.NotificationService;
import com.inkfront.logisticsApplication.service.interfaces.vehicle.VehicleMaintenanceService;
import com.inkfront.logisticsApplication.validator.vehicle.VehicleMaintenanceValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class VehicleMaintenanceServiceImpl implements VehicleMaintenanceService {

    private final VehicleMaintenanceRepository maintenanceRepository;
    private final VehicleRepository vehicleRepository;
    private final VehicleMapper vehicleMapper;
    private final VehicleMaintenanceValidator validator;
    private final NotificationService notificationService;
    private final AuditService auditService;

    @Override
    public VehicleMaintenanceDTO scheduleMaintenance(String vehicleId, VehicleMaintenanceRequestDTO request, String userId) {
        log.info("Scheduling maintenance for vehicle {}", vehicleId);

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle not found"));

        validator.validateVehicleCanGoForMaintenance(vehicle);

        VehicleMaintenance maintenance = new VehicleMaintenance();
        maintenance.setVehicle(vehicle);
        maintenance.setScheduledDate(request.getScheduledDate() != null ? request.getScheduledDate() : LocalDate.now());
        maintenance.setType(request.getType());
        maintenance.setDescription(request.getDescription());
        maintenance.setStatus(request.getStatus() != null ? request.getStatus() : MaintenanceStatus.SCHEDULED);
        maintenance.setCost(request.getCost());
        maintenance.setServiceProvider(request.getServiceProvider());
        maintenance.setOdometerReading(request.getOdometerReading());
        maintenance.setNotes(request.getNotes());

        // If status is IN_PROGRESS, update vehicle status
        if (maintenance.getStatus() == MaintenanceStatus.IN_PROGRESS) {
            vehicle.startMaintenance();
            vehicleRepository.save(vehicle);
        }

        maintenance = maintenanceRepository.save(maintenance);

        auditService.logAction(userId, "MAINTENANCE_SCHEDULED", "VehicleMaintenance", maintenance.getId(),
                "Scheduled " + maintenance.getType() + " for vehicle " + vehicle.getVehicleNumber());

        notificationService.sendSystemNotification(userId, "Maintenance Scheduled",
                "Maintenance for vehicle " + vehicle.getVehicleNumber() + " scheduled for " + maintenance.getScheduledDate());

        return vehicleMapper.toMaintenanceDTO(maintenance);
    }

    @Override
    public VehicleMaintenanceDTO updateMaintenance(String maintenanceId, VehicleMaintenanceRequestDTO request, String userId) {
        log.info("Updating maintenance {}", maintenanceId);

        VehicleMaintenance maintenance = maintenanceRepository.findById(maintenanceId)
                .orElseThrow(() -> new IllegalArgumentException("Maintenance record not found"));

        maintenance.setScheduledDate(request.getScheduledDate());
        maintenance.setType(request.getType());
        maintenance.setDescription(request.getDescription());
        maintenance.setStatus(request.getStatus());
        maintenance.setCost(request.getCost());
        maintenance.setServiceProvider(request.getServiceProvider());
        maintenance.setOdometerReading(request.getOdometerReading());
        maintenance.setNotes(request.getNotes());

        // If status changed to IN_PROGRESS, update vehicle
        if (request.getStatus() == MaintenanceStatus.IN_PROGRESS) {
            Vehicle vehicle = maintenance.getVehicle();
            vehicle.startMaintenance();
            vehicleRepository.save(vehicle);
        }

        maintenance = maintenanceRepository.save(maintenance);

        auditService.logAction(userId, "MAINTENANCE_UPDATED", "VehicleMaintenance", maintenance.getId(),
                "Updated maintenance for vehicle " + maintenance.getVehicle().getVehicleNumber());

        return vehicleMapper.toMaintenanceDTO(maintenance);
    }

    @Override
    public VehicleMaintenanceDTO completeMaintenance(String maintenanceId, String userId) {
        log.info("Completing maintenance {}", maintenanceId);

        VehicleMaintenance maintenance = maintenanceRepository.findById(maintenanceId)
                .orElseThrow(() -> new IllegalArgumentException("Maintenance record not found"));

        if (maintenance.getStatus() == MaintenanceStatus.COMPLETED) {
            throw new MaintenanceException("Maintenance already completed");
        }

        maintenance.setStatus(MaintenanceStatus.COMPLETED);
        maintenance.setCompletedDate(LocalDate.now());

        Vehicle vehicle = maintenance.getVehicle();
        vehicle.completeMaintenance();
        vehicle.setLastMaintenanceDate(LocalDate.now());
        // Set next maintenance date – e.g., 6 months from now or based on mileage
        vehicle.setNextMaintenanceDate(LocalDate.now().plusMonths(6));
        vehicleRepository.save(vehicle);

        maintenance = maintenanceRepository.save(maintenance);

        auditService.logAction(userId, "MAINTENANCE_COMPLETED", "VehicleMaintenance", maintenance.getId(),
                "Completed maintenance for vehicle " + vehicle.getVehicleNumber());

        notificationService.sendSystemNotification(userId, "Maintenance Completed",
                "Maintenance for vehicle " + vehicle.getVehicleNumber() + " completed successfully");

        return vehicleMapper.toMaintenanceDTO(maintenance);
    }

    @Override
    public VehicleMaintenanceDTO getMaintenanceById(String maintenanceId) {
        VehicleMaintenance maintenance = maintenanceRepository.findById(maintenanceId)
                .orElseThrow(() -> new IllegalArgumentException("Maintenance record not found"));
        return vehicleMapper.toMaintenanceDTO(maintenance);
    }

    @Override
    public List<VehicleMaintenanceDTO> getMaintenanceByVehicle(String vehicleId) {
        List<VehicleMaintenance> list = maintenanceRepository.findByVehicleIdOrderByScheduledDateDesc(vehicleId);
        return list.stream().map(vehicleMapper::toMaintenanceDTO).collect(Collectors.toList());
    }

    @Override
    public PaginatedResponseDTO<VehicleMaintenanceDTO> getMaintenanceHistory(String vehicleId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<VehicleMaintenance> pageResult = maintenanceRepository.findByVehicleId(vehicleId, pageable);
        List<VehicleMaintenanceDTO> content = pageResult.getContent().stream()
                .map(vehicleMapper::toMaintenanceDTO)
                .collect(Collectors.toList());
        return new PaginatedResponseDTO<>(content, pageResult.getNumber(), pageResult.getSize(), pageResult.getTotalElements());
    }

    @Override
    public void deleteMaintenance(String maintenanceId, String userId) {
        log.info("Deleting maintenance {}", maintenanceId);
        VehicleMaintenance maintenance = maintenanceRepository.findById(maintenanceId)
                .orElseThrow(() -> new IllegalArgumentException("Maintenance record not found"));

        if (maintenance.getStatus() == MaintenanceStatus.COMPLETED || maintenance.getStatus() == MaintenanceStatus.IN_PROGRESS) {
            throw new MaintenanceException("Cannot delete completed or in-progress maintenance");
        }

        maintenanceRepository.delete(maintenance);

        auditService.logAction(userId, "MAINTENANCE_DELETED", "VehicleMaintenance", maintenanceId,
                "Deleted maintenance record");
    }
}