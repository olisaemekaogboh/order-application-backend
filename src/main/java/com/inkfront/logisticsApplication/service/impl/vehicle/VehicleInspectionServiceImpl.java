package com.inkfront.logisticsApplication.service.impl.vehicle;

import com.inkfront.logisticsApplication.domain.entity.vehicle.Vehicle;
import com.inkfront.logisticsApplication.domain.entity.vehicle.VehicleInspection;
import com.inkfront.logisticsApplication.dto.request.vehicle.VehicleInspectionRequestDTO;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import com.inkfront.logisticsApplication.dto.response.vehicle.VehicleInspectionDTO;
import com.inkfront.logisticsApplication.exception.vehicle.InspectionException;
import com.inkfront.logisticsApplication.exception.vehicle.VehicleNotFoundException;
import com.inkfront.logisticsApplication.mapper.vehicle.VehicleMapper;
import com.inkfront.logisticsApplication.repository.vehicle.VehicleInspectionRepository;
import com.inkfront.logisticsApplication.repository.vehicle.VehicleRepository;
import com.inkfront.logisticsApplication.service.interfaces.AuditService;
import com.inkfront.logisticsApplication.service.interfaces.NotificationService;
import com.inkfront.logisticsApplication.service.interfaces.vehicle.VehicleInspectionService;
import com.inkfront.logisticsApplication.validator.vehicle.InspectionValidator;
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
public class VehicleInspectionServiceImpl implements VehicleInspectionService {

    private final VehicleInspectionRepository inspectionRepository;
    private final VehicleRepository vehicleRepository;
    private final VehicleMapper vehicleMapper;
    private final InspectionValidator validator;
    private final NotificationService notificationService;
    private final AuditService auditService;

    @Override
    public VehicleInspectionDTO scheduleInspection(String vehicleId, VehicleInspectionRequestDTO request, String userId) {
        log.info("Scheduling inspection for vehicle {}", vehicleId);

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle not found"));

        validator.validateVehicleCanBeInspected(vehicle);

        VehicleInspection inspection = new VehicleInspection();
        inspection.setVehicle(vehicle);
        inspection.setInspectionDate(request.getInspectionDate() != null ? request.getInspectionDate() : LocalDate.now());
        inspection.setInspectorName(request.getInspectorName());
        inspection.setResult(request.getResult());
        inspection.setRemarks(request.getRemarks());
        inspection.setNextInspectionDate(request.getNextInspectionDate());
        inspection.setCertificateNumber(request.getCertificateNumber());
        inspection.setCompliant(request.isCompliant());

        // Update vehicle next inspection date
        if (request.getNextInspectionDate() != null) {
            vehicle.setNextInspectionDate(request.getNextInspectionDate());
            vehicleRepository.save(vehicle);
        }

        inspection = inspectionRepository.save(inspection);

        auditService.logAction(userId, "INSPECTION_SCHEDULED", "VehicleInspection", inspection.getId(),
                "Scheduled inspection for vehicle " + vehicle.getVehicleNumber());

        notificationService.sendSystemNotification(userId, "Inspection Scheduled",
                "Inspection for vehicle " + vehicle.getVehicleNumber() + " scheduled for " + inspection.getInspectionDate());

        return vehicleMapper.toInspectionDTO(inspection);
    }

    @Override
    public VehicleInspectionDTO updateInspection(String inspectionId, VehicleInspectionRequestDTO request, String userId) {
        log.info("Updating inspection {}", inspectionId);

        VehicleInspection inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new IllegalArgumentException("Inspection record not found"));

        inspection.setInspectionDate(request.getInspectionDate());
        inspection.setInspectorName(request.getInspectorName());
        inspection.setResult(request.getResult());
        inspection.setRemarks(request.getRemarks());
        inspection.setNextInspectionDate(request.getNextInspectionDate());
        inspection.setCertificateNumber(request.getCertificateNumber());
        inspection.setCompliant(request.isCompliant());

        inspection = inspectionRepository.save(inspection);

        auditService.logAction(userId, "INSPECTION_UPDATED", "VehicleInspection", inspection.getId(),
                "Updated inspection for vehicle " + inspection.getVehicle().getVehicleNumber());

        return vehicleMapper.toInspectionDTO(inspection);
    }

    @Override
    public VehicleInspectionDTO getInspectionById(String inspectionId) {
        VehicleInspection inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new IllegalArgumentException("Inspection record not found"));
        return vehicleMapper.toInspectionDTO(inspection);
    }

    @Override
    public List<VehicleInspectionDTO> getInspectionsByVehicle(String vehicleId) {
        List<VehicleInspection> list = inspectionRepository.findByVehicleIdOrderByInspectionDateDesc(vehicleId);
        return list.stream().map(vehicleMapper::toInspectionDTO).collect(Collectors.toList());
    }

    @Override
    public PaginatedResponseDTO<VehicleInspectionDTO> getInspectionHistory(String vehicleId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<VehicleInspection> pageResult = inspectionRepository.findByVehicleId(vehicleId, pageable);
        List<VehicleInspectionDTO> content = pageResult.getContent().stream()
                .map(vehicleMapper::toInspectionDTO)
                .collect(Collectors.toList());
        return new PaginatedResponseDTO<>(content, pageResult.getNumber(), pageResult.getSize(), pageResult.getTotalElements());
    }

    @Override
    public void deleteInspection(String inspectionId, String userId) {
        log.info("Deleting inspection {}", inspectionId);
        VehicleInspection inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new IllegalArgumentException("Inspection record not found"));

        inspectionRepository.delete(inspection);

        auditService.logAction(userId, "INSPECTION_DELETED", "VehicleInspection", inspectionId,
                "Deleted inspection record");
    }
}