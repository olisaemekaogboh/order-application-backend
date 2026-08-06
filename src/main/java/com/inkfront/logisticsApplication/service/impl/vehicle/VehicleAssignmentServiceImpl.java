package com.inkfront.logisticsApplication.service.impl.vehicle;

import com.inkfront.logisticsApplication.domain.entity.Driver;
import com.inkfront.logisticsApplication.domain.entity.vehicle.Vehicle;
import com.inkfront.logisticsApplication.domain.entity.vehicle.VehicleAssignment;
import com.inkfront.logisticsApplication.dto.request.vehicle.VehicleAssignmentRequestDTO;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import com.inkfront.logisticsApplication.dto.response.vehicle.VehicleAssignmentDTO;
import com.inkfront.logisticsApplication.events.publisher.VehicleEventPublisher;
import com.inkfront.logisticsApplication.exception.vehicle.VehicleNotFoundException;
import com.inkfront.logisticsApplication.mapper.vehicle.VehicleMapper;
import com.inkfront.logisticsApplication.repository.DriverRepository;
import com.inkfront.logisticsApplication.repository.vehicle.VehicleAssignmentRepository;
import com.inkfront.logisticsApplication.repository.vehicle.VehicleRepository;
import com.inkfront.logisticsApplication.service.interfaces.AuditService;
import com.inkfront.logisticsApplication.service.interfaces.NotificationService;
import com.inkfront.logisticsApplication.service.interfaces.vehicle.VehicleAssignmentService;
import com.inkfront.logisticsApplication.validator.vehicle.VehicleAssignmentValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class VehicleAssignmentServiceImpl implements VehicleAssignmentService {

    private final VehicleAssignmentRepository assignmentRepository;
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final VehicleMapper vehicleMapper;
    private final VehicleAssignmentValidator validator;
    private final VehicleEventPublisher eventPublisher;
    private final NotificationService notificationService;
    private final AuditService auditService;

    @Override
    public VehicleAssignmentDTO assignDriver(String vehicleId, VehicleAssignmentRequestDTO request, String userId) {
        log.info("Assigning driver {} to vehicle {}", request.getDriverId(), vehicleId);

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle not found"));

        Driver driver = driverRepository.findById(request.getDriverId())
                .orElseThrow(() -> new IllegalArgumentException("Driver not found"));

        validator.validateVehicleAvailable(vehicle);
        validator.validateDriverNotAlreadyAssigned(driver);

        // Create assignment
        VehicleAssignment assignment = new VehicleAssignment();
        assignment.setVehicle(vehicle);
        assignment.setDriver(driver);
        assignment.setAssignedAt(LocalDateTime.now());
        assignment.setActive(true);
        assignment.setAssignmentReason(request.getAssignmentReason());
        assignment.setNotes(request.getNotes());

        // Update vehicle status
        vehicle.assign();
        vehicleRepository.save(vehicle);

        assignment = assignmentRepository.save(assignment);

        eventPublisher.publishVehicleAssigned(vehicle);

        auditService.logAction(userId, "VEHICLE_ASSIGNED", "VehicleAssignment", assignment.getId(),
                "Assigned driver " + driver.getUser().getFullName() + " to vehicle " + vehicle.getVehicleNumber());

        notificationService.sendSystemNotification(driver.getId(), "Vehicle Assigned",
                "You have been assigned to vehicle " + vehicle.getVehicleNumber());
        notificationService.sendSystemNotification(userId, "Vehicle Assignment",
                "Vehicle " + vehicle.getVehicleNumber() + " assigned to " + driver.getUser().getFullName());

        return vehicleMapper.toAssignmentDTO(assignment);
    }

    @Override
    public VehicleAssignmentDTO releaseDriver(String vehicleId, String reason, String userId) {
        log.info("Releasing driver from vehicle {}", vehicleId);

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle not found"));

        VehicleAssignment assignment = assignmentRepository.findByVehicleIdAndActiveTrue(vehicleId)
                .orElseThrow(() -> new IllegalStateException("Vehicle is not currently assigned"));

        assignment.setActive(false);
        assignment.setReleasedAt(LocalDateTime.now());
        assignment.setReleaseReason(reason);
        assignmentRepository.save(assignment);

        // Update vehicle status
        vehicle.release();
        vehicleRepository.save(vehicle);

        eventPublisher.publishVehicleReleased(vehicle);

        auditService.logAction(userId, "VEHICLE_RELEASED", "VehicleAssignment", assignment.getId(),
                "Released vehicle " + vehicle.getVehicleNumber() + " from driver " + assignment.getDriver().getUser().getFullName());

        notificationService.sendSystemNotification(assignment.getDriver().getId(), "Vehicle Released",
                "You have been released from vehicle " + vehicle.getVehicleNumber());

        return vehicleMapper.toAssignmentDTO(assignment);
    }

    @Override
    public VehicleAssignmentDTO getCurrentAssignment(String vehicleId) {
        VehicleAssignment assignment = assignmentRepository.findByVehicleIdAndActiveTrue(vehicleId)
                .orElse(null);
        return assignment != null ? vehicleMapper.toAssignmentDTO(assignment) : null;
    }

    @Override
    public List<VehicleAssignmentDTO> getAssignmentHistory(String vehicleId) {
        List<VehicleAssignment> assignments = assignmentRepository.findByVehicleIdOrderByAssignedAtDesc(vehicleId);
        return assignments.stream()
                .map(vehicleMapper::toAssignmentDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PaginatedResponseDTO<VehicleAssignmentDTO> getAssignmentsByVehicle(String vehicleId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<VehicleAssignment> pageResult = assignmentRepository.findByVehicleId(vehicleId, pageable);
        List<VehicleAssignmentDTO> content = pageResult.getContent().stream()
                .map(vehicleMapper::toAssignmentDTO)
                .collect(Collectors.toList());
        return new PaginatedResponseDTO<>(content, pageResult.getNumber(), pageResult.getSize(), pageResult.getTotalElements());
    }

    @Override
    public PaginatedResponseDTO<VehicleAssignmentDTO> getAssignmentsByDriver(String driverId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<VehicleAssignment> pageResult = assignmentRepository.findByDriverId(driverId, pageable);
        List<VehicleAssignmentDTO> content = pageResult.getContent().stream()
                .map(vehicleMapper::toAssignmentDTO)
                .collect(Collectors.toList());
        return new PaginatedResponseDTO<>(content, pageResult.getNumber(), pageResult.getSize(), pageResult.getTotalElements());
    }
}