package com.inkfront.logisticsApplication.service.interfaces;

import com.inkfront.logisticsApplication.dto.request.driver.*;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import com.inkfront.logisticsApplication.dto.response.driver.DriverDTO;
import com.inkfront.logisticsApplication.dto.response.driver.DriverEarningDTO;

import java.util.List;

public interface DriverService {







    DriverDTO registerDriver(
            DriverRegistrationRequestDTO registrationRequest);

    DriverDTO updateDriver(
            String driverId,
            DriverUpdateRequestDTO updateRequest);

    DriverDTO getDriverById(
            String driverId);

    DriverDTO getMyProfile(
            String driverId);

    DriverDTO getDriverByEmail(
            String email);

    PaginatedResponseDTO<DriverDTO> getAllDrivers(
            int page,
            int size,
            String sortBy,
            String sortDirection);

    PaginatedResponseDTO<DriverDTO> getAvailableDrivers(
            int page,
            int size);

    List<DriverDTO> getAvailableDriversForAssignment(
            String vehicleType);

    void deleteDriver(
            String driverId);
    DriverDTO updateAvailability(
            String driverId,
            DriverAvailabilityRequestDTO request
    );

    DriverDTO updateLocation(
            String driverId,
            DriverLocationRequestDTO request
    );

    List<DriverEarningDTO> getDriverEarnings(
            String driverId);

    PaginatedResponseDTO<DriverEarningDTO> getDriverEarningsPaginated(
            String driverId,
            int page,
            int size);

    Double getDriverTotalEarnings(
            String driverId);

    Double getDriverUnpaidEarnings(
            String driverId);

    void processDriverPayment(
            String driverId,
            Double amount);

    long countTotalDrivers();

    long countAvailableDrivers();

    double getAverageDriverRating();



}