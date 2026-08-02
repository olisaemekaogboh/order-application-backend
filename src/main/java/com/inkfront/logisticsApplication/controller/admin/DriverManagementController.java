package com.inkfront.logisticsApplication.controller.admin;

import com.inkfront.logisticsApplication.dto.request.driver.DriverRegistrationRequestDTO;
import com.inkfront.logisticsApplication.dto.request.driver.DriverUpdateRequestDTO;
import com.inkfront.logisticsApplication.dto.response.common.ApiResponseDTO;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import com.inkfront.logisticsApplication.dto.response.driver.DriverDTO;
import com.inkfront.logisticsApplication.service.interfaces.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/drivers")
@RequiredArgsConstructor
@Tag(name = "Admin Driver Management", description = "Admin driver management endpoints")
@PreAuthorize("hasRole('ADMIN')")
public class DriverManagementController {

    private final DriverService driverService;

    // ✅ NEW: Admin can register a driver
    @PostMapping
    @Operation(summary = "Register a new driver (admin)")
    public ResponseEntity<ApiResponseDTO<DriverDTO>> registerDriver(
            @Valid @RequestBody DriverRegistrationRequestDTO registrationRequest) {
        log.info("Admin registering driver: {}", registrationRequest.getEmail());
        DriverDTO response = driverService.registerDriver(registrationRequest);
        return ResponseEntity.ok(ApiResponseDTO.success("Driver registered successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all drivers (admin)")
    public ResponseEntity<ApiResponseDTO<PaginatedResponseDTO<DriverDTO>>> getAllDrivers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean available) {

        log.info("Admin get all drivers request - page: {}, size: {}, search: {}, available: {}",
                page, size, search, available);

        PaginatedResponseDTO<DriverDTO> response;
        if (search != null && !search.isEmpty()) {
            response = driverService.searchDrivers(search, page, size);
        } else if (available != null) {
            if (available) {
                response = driverService.getAvailableDrivers(page, size);
            } else {
                response = driverService.getUnavailableDrivers(page, size);
            }
        } else {
            response = driverService.getAllDrivers(page, size, sortBy, sortDirection);
        }

        return ResponseEntity.ok(ApiResponseDTO.success("Drivers retrieved successfully", response));
    }

    @GetMapping("/{driverId}")
    @Operation(summary = "Get driver by ID (admin)")
    public ResponseEntity<ApiResponseDTO<DriverDTO>> getDriverById(@PathVariable String driverId) {
        log.info("Admin get driver by ID request for: {}", driverId);
        DriverDTO response = driverService.getDriverById(driverId);
        return ResponseEntity.ok(ApiResponseDTO.success("Driver retrieved successfully", response));
    }

    @PutMapping("/{driverId}")
    @Operation(summary = "Update driver (admin)")
    public ResponseEntity<ApiResponseDTO<DriverDTO>> updateDriver(
            @PathVariable String driverId,
            @Valid @RequestBody DriverUpdateRequestDTO updateRequest) {
        log.info("Admin update driver request for: {}", driverId);
        DriverDTO response = driverService.updateDriver(driverId, updateRequest);
        return ResponseEntity.ok(ApiResponseDTO.success("Driver updated successfully", response));
    }

    @DeleteMapping("/{driverId}")
    @Operation(summary = "Delete driver (admin)")
    public ResponseEntity<ApiResponseDTO<Void>> deleteDriver(@PathVariable String driverId) {
        log.info("Admin delete driver request for: {}", driverId);
        driverService.deleteDriver(driverId);
        return ResponseEntity.ok(ApiResponseDTO.success("Driver deleted successfully", null));
    }

    @PutMapping("/{driverId}/availability")
    @Operation(summary = "Update driver availability (admin)")
    public ResponseEntity<ApiResponseDTO<DriverDTO>> updateDriverAvailability(
            @PathVariable String driverId,
            @RequestParam Boolean available) {
        log.info("Admin update availability for driver {} to {}", driverId, available);
        DriverDTO response = driverService.updateAvailability(driverId, available);
        return ResponseEntity.ok(ApiResponseDTO.success("Driver availability updated", response));
    }

    @PostMapping("/{driverId}/payments")
    @Operation(summary = "Process driver payment (admin)")
    public ResponseEntity<ApiResponseDTO<Void>> processDriverPayment(
            @PathVariable String driverId,
            @RequestParam Double amount) {
        log.info("Admin processing payment for driver {}: ${}", driverId, amount);
        driverService.processDriverPayment(driverId, amount);
        return ResponseEntity.ok(ApiResponseDTO.success("Driver payment processed successfully", null));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get driver statistics (admin)")
    public ResponseEntity<ApiResponseDTO<DriverStatsDTO>> getDriverStats() {
        log.info("Admin get driver stats request");
        DriverStatsDTO stats = new DriverStatsDTO();
        stats.setTotalDrivers(driverService.countTotalDrivers());
        stats.setAvailableDrivers(driverService.countAvailableDrivers());
        stats.setAverageRating(driverService.getAverageDriverRating());
        return ResponseEntity.ok(ApiResponseDTO.success("Driver stats retrieved", stats));
    }

    @lombok.Data
    private static class DriverStatsDTO {
        private long totalDrivers;
        private long availableDrivers;
        private double averageRating;
    }
    @PutMapping("/{driverId}/verify")
    @Operation(summary = "Verify driver (admin)")
    public ResponseEntity<ApiResponseDTO<DriverDTO>> verifyDriver(
            @PathVariable String driverId) {
        log.info("Admin verifying driver: {}", driverId);
        DriverDTO response = driverService.verifyDriver(driverId);
        return ResponseEntity.ok(ApiResponseDTO.success("Driver verified successfully", response));
    }
}