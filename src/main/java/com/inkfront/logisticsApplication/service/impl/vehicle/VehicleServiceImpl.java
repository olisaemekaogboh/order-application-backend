package com.inkfront.logisticsApplication.service.impl.vehicle;

import com.inkfront.logisticsApplication.domain.entity.vehicle.Vehicle;
import com.inkfront.logisticsApplication.domain.entity.vehicle.VehicleAssignment;
import com.inkfront.logisticsApplication.domain.enums.VehicleStatus;
import com.inkfront.logisticsApplication.dto.request.vehicle.VehicleFilterRequestDTO;
import com.inkfront.logisticsApplication.dto.request.vehicle.VehicleRequestDTO;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import com.inkfront.logisticsApplication.dto.response.vehicle.VehicleResponseDTO;
import com.inkfront.logisticsApplication.dto.response.vehicle.VehicleSummaryDTO;
import com.inkfront.logisticsApplication.events.publisher.VehicleEventPublisher;
import com.inkfront.logisticsApplication.exception.vehicle.VehicleNotFoundException;
import com.inkfront.logisticsApplication.mapper.vehicle.VehicleMapper;
import com.inkfront.logisticsApplication.repository.vehicle.VehicleRepository;
import com.inkfront.logisticsApplication.repository.vehicle.VehicleAssignmentRepository;
import com.inkfront.logisticsApplication.service.interfaces.AuditService;
import com.inkfront.logisticsApplication.service.interfaces.NotificationService;
import com.inkfront.logisticsApplication.service.interfaces.vehicle.VehicleService;
import com.inkfront.logisticsApplication.validator.vehicle.VehicleValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleAssignmentRepository assignmentRepository;
    private final VehicleMapper vehicleMapper;
    private final VehicleValidator vehicleValidator;
    private final VehicleEventPublisher eventPublisher;
    private final NotificationService notificationService;
    private final AuditService auditService;

    @Override
    public VehicleResponseDTO createVehicle(VehicleRequestDTO request, String userId) {
        log.info("Creating vehicle with registration: {}", request.getRegistrationNumber());

        vehicleValidator.validateUniqueVehicleNumber(request.getVehicleNumber());
        vehicleValidator.validateUniqueRegistrationNumber(request.getRegistrationNumber());
        if (StringUtils.hasText(request.getVin())) {
            vehicleValidator.validateUniqueVin(request.getVin());
        }

        Vehicle vehicle = vehicleMapper.toEntity(request);
        vehicle.setStatus(VehicleStatus.AVAILABLE);
        vehicle = vehicleRepository.save(vehicle);

        eventPublisher.publishVehicleCreated(vehicle);

        auditService.logAction(userId, "VEHICLE_CREATED", "Vehicle", vehicle.getId(),
                "Created vehicle " + vehicle.getVehicleNumber());

        notificationService.sendSystemNotification(userId, "Vehicle Created",
                "Vehicle " + vehicle.getVehicleNumber() + " has been added to the fleet.");

        return vehicleMapper.toResponseDTO(vehicle);
    }

    @Override
    public VehicleResponseDTO updateVehicle(String vehicleId, VehicleRequestDTO request, String userId) {
        log.info("Updating vehicle: {}", vehicleId);

        Vehicle vehicle = findVehicle(vehicleId);

        // Check uniqueness of fields if changed
        if (!vehicle.getVehicleNumber().equals(request.getVehicleNumber())) {
            vehicleValidator.validateUniqueVehicleNumber(request.getVehicleNumber());
        }
        if (!vehicle.getRegistrationNumber().equals(request.getRegistrationNumber())) {
            vehicleValidator.validateUniqueRegistrationNumber(request.getRegistrationNumber());
        }
        if (StringUtils.hasText(request.getVin()) && !request.getVin().equals(vehicle.getVin())) {
            vehicleValidator.validateUniqueVin(request.getVin());
        }

        // Update fields manually or use mapper (but we need to preserve status and relationships)
        vehicle.setRegistrationNumber(request.getRegistrationNumber());
        vehicle.setPlateNumber(request.getPlateNumber());
        vehicle.setVin(request.getVin());
        vehicle.setEngineNumber(request.getEngineNumber());
        vehicle.setChassisNumber(request.getChassisNumber());
        vehicle.setManufacturer(request.getManufacturer());
        vehicle.setBrand(request.getBrand());
        vehicle.setModel(request.getModel());
        vehicle.setYear(request.getYear());
        vehicle.setVehicleType(request.getVehicleType());
        vehicle.setFuelType(request.getFuelType());
        vehicle.setTransmission(request.getTransmission());
        vehicle.setColor(request.getColor());
        vehicle.setCapacityKg(request.getCapacityKg());
        vehicle.setCapacityVolume(request.getCapacityVolume());
        vehicle.setMaxPassengers(request.getMaxPassengers());
        vehicle.setFuelConsumption(request.getFuelConsumption());
        vehicle.setPurchasePrice(request.getPurchasePrice());
        vehicle.setPurchaseDate(request.getPurchaseDate());
        vehicle.setInsuranceExpiry(request.getInsuranceExpiry());
        vehicle.setRoadWorthinessExpiry(request.getRoadWorthinessExpiry());
        vehicle.setLicenseExpiry(request.getLicenseExpiry());

        vehicle = vehicleRepository.save(vehicle);

        auditService.logAction(userId, "VEHICLE_UPDATED", "Vehicle", vehicle.getId(),
                "Updated vehicle " + vehicle.getVehicleNumber());

        return vehicleMapper.toResponseDTO(vehicle);
    }

    @Override
    public VehicleResponseDTO getVehicleById(String vehicleId) {
        Vehicle vehicle = findVehicle(vehicleId);
        return vehicleMapper.toResponseDTO(vehicle);
    }

    @Override
    public VehicleResponseDTO getVehicleByNumber(String vehicleNumber) {
        Vehicle vehicle = vehicleRepository.findByVehicleNumber(vehicleNumber)
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle not found with number: " + vehicleNumber));
        return vehicleMapper.toResponseDTO(vehicle);
    }

    @Override
    public PaginatedResponseDTO<VehicleSummaryDTO> searchVehicles(VehicleFilterRequestDTO filter) {
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(),
                Sort.by(Sort.Direction.fromString(filter.getSortDirection()), filter.getSortBy()));

        Page<Vehicle> page;

        // Build dynamic query – can be enhanced with Specifications
        if (StringUtils.hasText(filter.getKeyword())) {
            // Search by keyword across multiple fields (simplified)
            page = vehicleRepository.findAll(pageable); // placeholder – implement custom query
        } else {
            page = vehicleRepository.findAll(pageable);
        }

        // Apply filters in memory (simplified; better to use Specifications)
        List<Vehicle> filtered = page.getContent().stream()
                .filter(v -> filter.getStatus() == null || v.getStatus() == filter.getStatus())
                .filter(v -> filter.getVehicleType() == null || v.getVehicleType() == filter.getVehicleType())
                .filter(v -> filter.getBrand() == null || v.getBrand().equalsIgnoreCase(filter.getBrand()))
                .filter(v -> filter.getModel() == null || v.getModel().equalsIgnoreCase(filter.getModel()))
                .filter(v -> filter.getYear() == null || v.getYear().equals(filter.getYear()))
                .filter(v -> filter.getInsuranceExpiryBefore() == null || v.getInsuranceExpiry() != null &&
                        v.getInsuranceExpiry().isBefore(filter.getInsuranceExpiryBefore()))
                .filter(v -> filter.getInspectionDueBefore() == null || v.getNextInspectionDate() != null &&
                        v.getNextInspectionDate().isBefore(filter.getInspectionDueBefore()))
                .filter(v -> filter.getAvailable() == null ||
                        (filter.getAvailable() ? v.isAvailable() : !v.isAvailable()))
                .collect(Collectors.toList());

        List<VehicleSummaryDTO> content = filtered.stream()
                .map(vehicleMapper::toSummaryDTO)
                .collect(Collectors.toList());

        return new PaginatedResponseDTO<>(content, filter.getPage(), filter.getSize(), (long) filtered.size());
    }

    @Override
    public List<VehicleSummaryDTO> getVehiclesByStatus(String status) {
        VehicleStatus vehicleStatus = VehicleStatus.valueOf(status.toUpperCase());
        List<Vehicle> vehicles = vehicleRepository.findByStatusAndDeletedFalse(vehicleStatus);
        return vehicles.stream()
                .map(vehicleMapper::toSummaryDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteVehicle(String vehicleId, String userId) {
        log.info("Deleting vehicle: {}", vehicleId);

        Vehicle vehicle = findVehicle(vehicleId);

        // Check if vehicle is currently assigned
        if (assignmentRepository.existsByVehicleIdAndActiveTrue(vehicleId)) {
            throw new IllegalStateException("Cannot delete vehicle that is currently assigned");
        }

        vehicle.setDeleted(true);
        vehicle.setStatus(VehicleStatus.RETIRED);
        vehicleRepository.save(vehicle);

        auditService.logAction(userId, "VEHICLE_DELETED", "Vehicle", vehicle.getId(),
                "Deleted vehicle " + vehicle.getVehicleNumber());

        eventPublisher.publishVehicleRetired(vehicle);
        notificationService.sendSystemNotification(userId, "Vehicle Retired",
                "Vehicle " + vehicle.getVehicleNumber() + " has been retired and removed from fleet.");
    }

    @Override
    public VehicleResponseDTO updateVehicleStatus(String vehicleId, String status, String userId) {
        log.info("Updating vehicle status: {} to {}", vehicleId, status);

        Vehicle vehicle = findVehicle(vehicleId);
        VehicleStatus newStatus = VehicleStatus.valueOf(status.toUpperCase());

        // Validate transition (simplified)
        if (vehicle.getStatus() == VehicleStatus.RETIRED) {
            throw new IllegalStateException("Cannot change status of a retired vehicle");
        }

        vehicle.setStatus(newStatus);
        vehicle = vehicleRepository.save(vehicle);

        auditService.logAction(userId, "VEHICLE_STATUS_CHANGED", "Vehicle", vehicle.getId(),
                "Changed status to " + newStatus + " for vehicle " + vehicle.getVehicleNumber());

        return vehicleMapper.toResponseDTO(vehicle);
    }

    // Helper
    private Vehicle findVehicle(String vehicleId) {
        return vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle not found with ID: " + vehicleId));
    }
}