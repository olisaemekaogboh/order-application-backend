package com.inkfront.logisticsApplication.service.impl;

import com.inkfront.logisticsApplication.domain.entity.Driver;
import com.inkfront.logisticsApplication.domain.entity.DriverEarning;
import com.inkfront.logisticsApplication.domain.enums.VehicleType;
import com.inkfront.logisticsApplication.dto.request.driver.DriverRegistrationRequestDTO;
import com.inkfront.logisticsApplication.dto.request.driver.DriverUpdateRequestDTO;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import com.inkfront.logisticsApplication.dto.response.driver.DriverDTO;
import com.inkfront.logisticsApplication.dto.response.driver.DriverEarningDTO;
import com.inkfront.logisticsApplication.exception.BadRequestException;
import com.inkfront.logisticsApplication.exception.DuplicateResourceException;
import com.inkfront.logisticsApplication.exception.ResourceNotFoundException;
import com.inkfront.logisticsApplication.mapper.DriverMapper;
import com.inkfront.logisticsApplication.mapper.DriverEarningMapper;
import com.inkfront.logisticsApplication.repository.DriverRepository;
import com.inkfront.logisticsApplication.repository.DriverEarningRepository;
import com.inkfront.logisticsApplication.service.interfaces.DriverService;
import com.inkfront.logisticsApplication.domain.constants.ErrorMessages;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;
    private final DriverEarningRepository driverEarningRepository;
    private final DriverMapper driverMapper;
    private final DriverEarningMapper driverEarningMapper;

    @Override
    public DriverDTO registerDriver(DriverRegistrationRequestDTO registrationRequest) {
        log.info("Registering new driver: {}", registrationRequest.getEmail());

        // Check for duplicates
        if (driverRepository.existsByEmail(registrationRequest.getEmail())) {
            throw new DuplicateResourceException("Driver with email " + registrationRequest.getEmail() + " already exists");
        }

        if (driverRepository.existsByPhoneNumber(registrationRequest.getPhoneNumber())) {
            throw new DuplicateResourceException("Driver with phone number " + registrationRequest.getPhoneNumber() + " already exists");
        }

        if (driverRepository.existsByLicenseNumber(registrationRequest.getLicenseNumber())) {
            throw new DuplicateResourceException("Driver with license number " + registrationRequest.getLicenseNumber() + " already exists");
        }

        if (driverRepository.existsByVehiclePlateNumber(registrationRequest.getVehiclePlateNumber())) {
            throw new DuplicateResourceException("Vehicle with plate number " + registrationRequest.getVehiclePlateNumber() + " already exists");
        }

        Driver driver = driverMapper.toEntity(registrationRequest);
        driver = driverRepository.save(driver);

        log.info("Driver registered successfully: {}", driver.getId());
        return driverMapper.toDTO(driver);
    }

    @Override
    public DriverDTO updateDriver(
            String driverId,
            DriverUpdateRequestDTO updateRequest) {

        log.info("Updating driver: {}", driverId);

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                ErrorMessages.DRIVER_NOT_FOUND));

        if (updateRequest.getPhoneNumber() != null
                && !updateRequest.getPhoneNumber().equals(driver.getPhoneNumber())
                && driverRepository.existsByPhoneNumber(updateRequest.getPhoneNumber())) {

            throw new DuplicateResourceException(
                    "Phone number already exists");
        }

        if (updateRequest.getVehiclePlateNumber() != null
                && !updateRequest.getVehiclePlateNumber().equals(driver.getVehiclePlateNumber())
                && driverRepository.existsByVehiclePlateNumber(updateRequest.getVehiclePlateNumber())) {

            throw new DuplicateResourceException(
                    "Vehicle plate number already exists");
        }

        driverMapper.updateDriverFromDTO(updateRequest, driver);

        driver = driverRepository.save(driver);

        return driverMapper.toDTO(driver);
    }
    @Override
    public DriverDTO getDriverById(String driverId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.DRIVER_NOT_FOUND));
        return driverMapper.toDTO(driver);
    }
    @Override
    public DriverDTO getMyProfile(String driverId) {

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                ErrorMessages.DRIVER_NOT_FOUND));

        return driverMapper.toDTO(driver);
    }
    @Override
    public DriverDTO getDriverByEmail(String email) {
        Driver driver = driverRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.DRIVER_NOT_FOUND));
        return driverMapper.toDTO(driver);
    }

    @Override
    public PaginatedResponseDTO<DriverDTO> getAllDrivers(int page, int size, String sortBy, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Driver> drivers = driverRepository.findAll(pageable);

        List<DriverDTO> content = drivers.getContent().stream()
                .map(driverMapper::toDTO)
                .collect(Collectors.toList());

        return new PaginatedResponseDTO<>(content, drivers.getNumber(), drivers.getSize(), drivers.getTotalElements());
    }

    @Override
    public PaginatedResponseDTO<DriverDTO> getAvailableDrivers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Driver> drivers = driverRepository.findByAvailableTrue(pageable);

        List<DriverDTO> content = drivers.getContent().stream()
                .map(driverMapper::toDTO)
                .collect(Collectors.toList());

        return new PaginatedResponseDTO<>(content, drivers.getNumber(), drivers.getSize(), drivers.getTotalElements());
    }

    @Override
    public List<DriverDTO> getAvailableDriversForAssignment(
            String vehicleType) {

        List<Driver> drivers;

        if (vehicleType == null || vehicleType.isBlank()) {

            drivers = driverRepository.findByAvailableTrueAndVerifiedTrue();

        } else {

            VehicleType type;

            try {
                type = VehicleType.valueOf(vehicleType.toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException(
                        "Invalid vehicle type: " + vehicleType);
            }

            drivers = driverRepository
                    .findByAvailableTrueAndVerifiedTrueAndVehicleType(type);
        }

        return drivers.stream()
                .map(driverMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteDriver(String driverId) {

        log.info("Deleting driver: {}", driverId);

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                ErrorMessages.DRIVER_NOT_FOUND));

        if (!driver.getAvailable()) {
            throw new BadRequestException(
                    "Driver cannot be deleted while assigned to active deliveries.");
        }

        driverRepository.delete(driver);
    }
    @Override
    public void updateAvailability(
            String driverId,
            boolean available) {

        log.info("Updating availability for driver: {}", driverId);

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                ErrorMessages.DRIVER_NOT_FOUND));

        if (!driver.getVerified()) {
            throw new BadRequestException(
                    "Driver must be verified before changing availability.");
        }

        driver.setAvailable(available);
        driver.setLastActive(LocalDateTime.now());

        driverRepository.save(driver);
    }
    @Override
    public void updateLocation(
            String driverId,
            Double latitude,
            Double longitude,
            String location) {

        log.info("Updating location for driver: {}", driverId);

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                ErrorMessages.DRIVER_NOT_FOUND));

        if (latitude == null || longitude == null) {
            throw new BadRequestException(
                    "Latitude and longitude are required.");
        }

        driver.setCurrentLatitude(latitude);
        driver.setCurrentLongitude(longitude);
        driver.setCurrentLocation(location);
        driver.setLastActive(LocalDateTime.now());

        driverRepository.save(driver);
    }
    @Override
    @Transactional(readOnly = true)
    public List<DriverEarningDTO> getDriverEarnings(String driverId) {

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                ErrorMessages.DRIVER_NOT_FOUND));

        return driverEarningRepository.findByDriverId(driver.getId())
                .stream()
                .map(driverEarningMapper::toDTO)
                .collect(Collectors.toList());
    }
    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDTO<DriverEarningDTO> getDriverEarningsPaginated(
            String driverId,
            int page,
            int size) {

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                ErrorMessages.DRIVER_NOT_FOUND));

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "earningDate")
        );

        Page<DriverEarning> earnings =
                driverEarningRepository.findByDriverId(
                        driver.getId(),
                        pageable);

        List<DriverEarningDTO> content =
                earnings.getContent()
                        .stream()
                        .map(driverEarningMapper::toDTO)
                        .collect(Collectors.toList());

        return new PaginatedResponseDTO<>(
                content,
                earnings.getNumber(),
                earnings.getSize(),
                earnings.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Double getDriverTotalEarnings(String driverId) {

        driverRepository.findById(driverId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                ErrorMessages.DRIVER_NOT_FOUND));

        return java.util.Optional
                .ofNullable(driverEarningRepository.sumTotalEarnings(driverId))
                .orElse(0.0);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getDriverUnpaidEarnings(String driverId) {

        driverRepository.findById(driverId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                ErrorMessages.DRIVER_NOT_FOUND));

        return java.util.Optional
                .ofNullable(driverEarningRepository.sumUnpaidEarnings(driverId))
                .orElse(0.0);
    }

    @Override
    public void processDriverPayment(
            String driverId,
            Double amount) {

        log.info(
                "Processing payment for driver: {}, amount: {}",
                driverId,
                amount);

        if (amount == null || amount <= 0) {
            throw new BadRequestException(
                    "Amount must be greater than zero.");
        }

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                ErrorMessages.DRIVER_NOT_FOUND));

        Double unpaid =
                getDriverUnpaidEarnings(driverId);

        if (amount > unpaid) {
            throw new BadRequestException(
                    "Payment exceeds unpaid earnings.");
        }

        List<DriverEarning> earnings =
                driverEarningRepository
                        .findByDriverIdAndPaidFalse(driverId);

        double remaining = amount;

        for (DriverEarning earning : earnings) {

            if (remaining <= 0) {
                break;
            }

            if (earning.getNetAmount() <= remaining) {

                earning.setPaid(true);
                earning.setPaidDate(LocalDateTime.now());

                remaining -= earning.getNetAmount();

            } else {

                earning.setNetAmount(
                        earning.getNetAmount() - remaining);

                earning.setAmount(
                        earning.getAmount() - remaining);

                remaining = 0;
            }

            driverEarningRepository.save(earning);
        }

        driver.setAvailableBalance(
                driver.getAvailableBalance() - amount);

        driverRepository.save(driver);
    }
    @Override
    public long countTotalDrivers() {
        return driverRepository.count();
    }

    @Override
    public long countAvailableDrivers() {
        return driverRepository.countAvailableDrivers();
    }

    @Override
    public double getAverageDriverRating() {
        Double avg = driverRepository.calculateAverageRating();
        return avg != null ? avg : 0.0;
    }
}