package com.inkfront.logisticsApplication.service.interfaces;

import com.inkfront.logisticsApplication.dto.request.driver.*;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import com.inkfront.logisticsApplication.dto.response.driver.DriverDTO;
import com.inkfront.logisticsApplication.dto.response.driver.DriverDashboardDTO;
import com.inkfront.logisticsApplication.dto.response.driver.DriverEarningDTO;

import java.util.List;

public interface DriverService {

    void updateDriverRatingStats(String driverId);

    DriverDTO registerDriver(DriverRegistrationRequestDTO registrationRequest);

    DriverDTO updateDriver(String driverId, DriverUpdateRequestDTO updateRequest);

    DriverDTO getDriverById(String driverId);

    DriverDTO getMyProfile(String driverId);

    DriverDTO getDriverByEmail(String email);

    PaginatedResponseDTO<DriverDTO> getAllDrivers(int page, int size, String sortBy, String sortDirection);

    PaginatedResponseDTO<DriverDTO> getAvailableDrivers(int page, int size);

    List<DriverDTO> getAvailableDriversForAssignment(String vehicleType);

    void deleteDriver(String driverId);

    DriverDTO updateAvailability(String driverId, DriverAvailabilityRequestDTO request);

    // NEW: Update availability with boolean (for admin)
    DriverDTO updateAvailability(String driverId, boolean available);

    DriverDTO updateLocation(String driverId, DriverLocationRequestDTO request);

    List<DriverEarningDTO> getDriverEarnings(String driverId);

    PaginatedResponseDTO<DriverEarningDTO> getDriverEarningsPaginated(String driverId, int page, int size);

    Double getDriverTotalEarnings(String driverId);

    Double getDriverUnpaidEarnings(String driverId);

    void processDriverPayment(String driverId, Double amount);

    long countTotalDrivers();

    long countAvailableDrivers();

    double getAverageDriverRating();

    // NEW: Search drivers
    PaginatedResponseDTO<DriverDTO> searchDrivers(String search, int page, int size);

    // NEW: Get unavailable drivers
    PaginatedResponseDTO<DriverDTO> getUnavailableDrivers(int page, int size);

    DriverDTO verifyDriver(String driverId);

    DriverDashboardDTO getDriverDashboard(String driverId);
}