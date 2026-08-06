package com.inkfront.logisticsApplication.controller.driver;

import com.inkfront.logisticsApplication.dto.request.driver.DriverAssignmentRequestDTO;
import com.inkfront.logisticsApplication.dto.request.driver.DriverAvailabilityRequestDTO;
import com.inkfront.logisticsApplication.dto.request.driver.DriverLocationRequestDTO;
import com.inkfront.logisticsApplication.dto.request.driver.DriverRegistrationRequestDTO;
import com.inkfront.logisticsApplication.dto.request.driver.DriverUpdateRequestDTO;
import com.inkfront.logisticsApplication.dto.response.common.ApiResponseDTO;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import com.inkfront.logisticsApplication.dto.response.driver.DriverDTO;
import com.inkfront.logisticsApplication.dto.response.driver.DriverDashboardDTO;
import com.inkfront.logisticsApplication.dto.response.driver.DriverEarningDTO;
import com.inkfront.logisticsApplication.service.interfaces.DriverService;
import com.inkfront.logisticsApplication.domain.constants.SuccessMessages;
import com.inkfront.logisticsApplication.security.AuthenticatedUser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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

    @GetMapping("/me")
    @Operation(summary = "Get current driver's profile")
    public ResponseEntity<ApiResponseDTO<DriverDTO>> getMyProfile(
            Authentication authentication) {

        AuthenticatedUser user =
                (AuthenticatedUser) authentication.getPrincipal();

        log.info("Get current driver profile: {}", user.getEmail());

        DriverDTO response =
                driverService.getMyProfile(user.getId());

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        SuccessMessages.DATA_RETRIEVED,
                        response
                )
        );
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

    @PutMapping("/me/availability")
    @Operation(summary = "Update current driver availability")
    public ResponseEntity<ApiResponseDTO<DriverDTO>> updateAvailability(
            Authentication authentication,
            @Valid @RequestBody DriverAvailabilityRequestDTO request) {

        AuthenticatedUser user =
                (AuthenticatedUser) authentication.getPrincipal();

        log.info("Update availability for driver: {}", user.getEmail());

        DriverDTO response =
                driverService.updateAvailability(
                        user.getId(),
                        request
                );

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        SuccessMessages.DRIVER_UPDATED,
                        response
                )
        );
    }

    @PutMapping("/me/location")
    @Operation(summary = "Update current driver location")
    public ResponseEntity<ApiResponseDTO<DriverDTO>> updateLocation(
            Authentication authentication,
            @Valid @RequestBody DriverLocationRequestDTO request) {

        AuthenticatedUser user =
                (AuthenticatedUser) authentication.getPrincipal();

        log.info("Update location for driver: {}", user.getEmail());

        DriverDTO response =
                driverService.updateLocation(
                        user.getId(),
                        request
                );

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        SuccessMessages.DRIVER_UPDATED,
                        response
                )
        );
    }

    @GetMapping("/me/earnings")
    @Operation(summary = "Get current driver earnings")
    public ResponseEntity<ApiResponseDTO<List<DriverEarningDTO>>> getDriverEarnings(
            Authentication authentication) {

        AuthenticatedUser user =
                (AuthenticatedUser) authentication.getPrincipal();

        log.info("Get earnings for driver: {}", user.getEmail());

        List<DriverEarningDTO> response =
                driverService.getDriverEarnings(
                        user.getId()
                );

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        SuccessMessages.DATA_RETRIEVED,
                        response
                )
        );
    }

    @GetMapping("/me/earnings/paginated")
    @Operation(summary = "Get current driver earnings paginated")
    public ResponseEntity<ApiResponseDTO<PaginatedResponseDTO<DriverEarningDTO>>> getDriverEarningsPaginated(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        AuthenticatedUser user =
                (AuthenticatedUser) authentication.getPrincipal();

        log.info("Get paginated earnings for driver: {}", user.getEmail());

        PaginatedResponseDTO<DriverEarningDTO> response =
                driverService.getDriverEarningsPaginated(
                        user.getId(),
                        page,
                        size
                );

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        SuccessMessages.DATA_RETRIEVED,
                        response
                )
        );
    }

    @GetMapping("/me/earnings/total")
    @Operation(summary = "Get current driver total earnings")
    public ResponseEntity<ApiResponseDTO<Double>> getDriverTotalEarnings(
            Authentication authentication) {

        AuthenticatedUser user =
                (AuthenticatedUser) authentication.getPrincipal();

        log.info("Get total earnings for driver: {}", user.getEmail());

        Double response =
                driverService.getDriverTotalEarnings(
                        user.getId()
                );

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        "Total earnings retrieved",
                        response
                )
        );
    }

    @GetMapping("/me/earnings/unpaid")
    public ResponseEntity<ApiResponseDTO<Double>> getMyUnpaidEarnings(
            Authentication authentication) {

        AuthenticatedUser user =
                (AuthenticatedUser) authentication.getPrincipal();

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        SuccessMessages.DATA_RETRIEVED,
                        driverService.getDriverUnpaidEarnings(user.getId())
                )
        );
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


    @GetMapping("/me/dashboard")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<ApiResponseDTO<DriverDashboardDTO>> getDashboard(
            Authentication authentication) {

        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        DriverDashboardDTO dashboard =
                driverService.getDriverDashboard(user.getId());

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        SuccessMessages.DATA_RETRIEVED,
                        dashboard
                )
        );
    }

    @Data
    private static class DriverStatsDTO {
        private long totalDrivers;
        private long availableDrivers;
        private double averageRating;
    }
}