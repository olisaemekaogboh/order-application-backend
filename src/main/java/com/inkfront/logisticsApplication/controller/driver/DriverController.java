package com.inkfront.logisticsApplication.controller.driver;

import com.inkfront.logisticsApplication.dto.request.driver.DriverAssignmentRequestDTO;
import com.inkfront.logisticsApplication.dto.request.driver.DriverRegistrationRequestDTO;
import com.inkfront.logisticsApplication.dto.request.driver.DriverUpdateRequestDTO;
import com.inkfront.logisticsApplication.dto.response.common.ApiResponseDTO;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import com.inkfront.logisticsApplication.dto.response.driver.DriverDTO;
import com.inkfront.logisticsApplication.dto.response.driver.DriverEarningDTO;
import com.inkfront.logisticsApplication.service.interfaces.DriverService;
import com.inkfront.logisticsApplication.domain.constants.SuccessMessages;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
@Tag(name = "Driver Management", description = "Driver management endpoints")
public class DriverController {

    private final DriverService driverService;

    @PostMapping
    @Operation(summary = "Register new driver")
    public ResponseEntity<ApiResponseDTO<DriverDTO>> registerDriver(
            @Valid @RequestBody DriverRegistrationRequestDTO registrationRequest) {
        log.info("Register driver request for email: {}", registrationRequest.getEmail());
        DriverDTO response = driverService.registerDriver(registrationRequest);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DRIVER_CREATED, response));
    }

    @PutMapping("/{driverId}")
    @Operation(summary = "Update driver")
    public ResponseEntity<ApiResponseDTO<DriverDTO>> updateDriver(
            @PathVariable String driverId,
            @Valid @RequestBody DriverUpdateRequestDTO updateRequest) {
        log.info("Update driver request for: {}", driverId);
        DriverDTO response = driverService.updateDriver(driverId, updateRequest);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DRIVER_UPDATED, response));
    }

    @GetMapping("/{driverId}")
    @Operation(summary = "Get driver by ID")
    public ResponseEntity<ApiResponseDTO<DriverDTO>> getDriverById(@PathVariable String driverId) {
        log.info("Get driver request for: {}", driverId);
        DriverDTO response = driverService.getDriverById(driverId);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Get driver by email")
    public ResponseEntity<ApiResponseDTO<DriverDTO>> getDriverByEmail(@PathVariable String email) {
        log.info("Get driver by email request for: {}", email);
        DriverDTO response = driverService.getDriverByEmail(email);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping
    @Operation(summary = "Get all drivers")
    public ResponseEntity<ApiResponseDTO<PaginatedResponseDTO<DriverDTO>>> getAllDrivers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        log.info("Get all drivers request");
        PaginatedResponseDTO<DriverDTO> response = driverService.getAllDrivers(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping("/available")
    @Operation(summary = "Get available drivers")
    public ResponseEntity<ApiResponseDTO<PaginatedResponseDTO<DriverDTO>>> getAvailableDrivers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Get available drivers request");
        PaginatedResponseDTO<DriverDTO> response = driverService.getAvailableDrivers(page, size);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping("/available/assignment")
    @Operation(summary = "Get available drivers for assignment")
    public ResponseEntity<ApiResponseDTO<List<DriverDTO>>> getAvailableDriversForAssignment(
            @RequestParam(required = false) String vehicleType) {
        log.info("Get available drivers for assignment request");
        List<DriverDTO> response = driverService.getAvailableDriversForAssignment(vehicleType);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @DeleteMapping("/{driverId}")
    @Operation(summary = "Delete driver")
    public ResponseEntity<ApiResponseDTO<Void>> deleteDriver(@PathVariable String driverId) {
        log.info("Delete driver request for: {}", driverId);
        driverService.deleteDriver(driverId);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DRIVER_DELETED, null));
    }

    @PutMapping("/{driverId}/availability")
    @Operation(summary = "Update driver availability")
    public ResponseEntity<ApiResponseDTO<Void>> updateAvailability(
            @PathVariable String driverId,
            @RequestParam boolean available) {
        log.info("Update driver availability request for: {} -> {}", driverId, available);
        driverService.updateAvailability(driverId, available);
        return ResponseEntity.ok(ApiResponseDTO.success("Driver availability updated successfully", null));
    }

    @PutMapping("/{driverId}/location")
    @Operation(summary = "Update driver location")
    public ResponseEntity<ApiResponseDTO<Void>> updateLocation(
            @PathVariable String driverId,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(required = false) String location) {
        log.info("Update driver location request for: {}", driverId);
        driverService.updateLocation(driverId, latitude, longitude, location);
        return ResponseEntity.ok(ApiResponseDTO.success("Driver location updated successfully", null));
    }

    @GetMapping("/{driverId}/earnings")
    @Operation(summary = "Get driver earnings")
    public ResponseEntity<ApiResponseDTO<List<DriverEarningDTO>>> getDriverEarnings(
            @PathVariable String driverId) {
        log.info("Get driver earnings request for: {}", driverId);
        List<DriverEarningDTO> response = driverService.getDriverEarnings(driverId);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping("/{driverId}/earnings/paginated")
    @Operation(summary = "Get driver earnings paginated")
    public ResponseEntity<ApiResponseDTO<PaginatedResponseDTO<DriverEarningDTO>>> getDriverEarningsPaginated(
            @PathVariable String driverId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Get driver earnings paginated request for: {}", driverId);
        PaginatedResponseDTO<DriverEarningDTO> response = driverService.getDriverEarningsPaginated(driverId, page, size);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping("/{driverId}/earnings/total")
    @Operation(summary = "Get driver total earnings")
    public ResponseEntity<ApiResponseDTO<Double>> getDriverTotalEarnings(@PathVariable String driverId) {
        log.info("Get driver total earnings request for: {}", driverId);
        Double response = driverService.getDriverTotalEarnings(driverId);
        return ResponseEntity.ok(ApiResponseDTO.success("Total earnings retrieved", response));
    }

    @GetMapping("/{driverId}/earnings/unpaid")
    @Operation(summary = "Get driver unpaid earnings")
    public ResponseEntity<ApiResponseDTO<Double>> getDriverUnpaidEarnings(@PathVariable String driverId) {
        log.info("Get driver unpaid earnings request for: {}", driverId);
        Double response = driverService.getDriverUnpaidEarnings(driverId);
        return ResponseEntity.ok(ApiResponseDTO.success("Unpaid earnings retrieved", response));
    }

    @PostMapping("/{driverId}/payments")
    @Operation(summary = "Process driver payment")
    public ResponseEntity<ApiResponseDTO<Void>> processDriverPayment(
            @PathVariable String driverId,
            @RequestParam Double amount) {
        log.info("Process driver payment request for: {}, amount: {}", driverId, amount);
        driverService.processDriverPayment(driverId, amount);
        return ResponseEntity.ok(ApiResponseDTO.success("Driver payment processed successfully", null));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get driver statistics")
    public ResponseEntity<ApiResponseDTO<DriverStatsDTO>> getDriverStats() {
        log.info("Get driver stats request");
        DriverStatsDTO stats = new DriverStatsDTO();
        stats.setTotalDrivers(driverService.countTotalDrivers());
        stats.setAvailableDrivers(driverService.countAvailableDrivers());
        stats.setAverageRating(driverService.getAverageDriverRating());
        return ResponseEntity.ok(ApiResponseDTO.success("Driver stats retrieved", stats));
    }

    @Data
    private static class DriverStatsDTO {
        private long totalDrivers;
        private long availableDrivers;
        private double averageRating;
    }
}