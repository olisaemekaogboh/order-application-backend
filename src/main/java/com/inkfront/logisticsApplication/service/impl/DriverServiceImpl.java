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
    public DriverDTO updateDriver(String driverId, DriverUpdateRequestDTO updateRequest) {
        log.info("Updating driver: {}", driverId);

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.DRIVER_NOT_FOUND));

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
    public List<DriverDTO> getAvailableDriversForAssignment(String vehicleType) {
        VehicleType type = VehicleType.valueOf(vehicleType);
        List<Driver> drivers = driverRepository.findByAvailableTrueAndVehicleType(type);
        return drivers.stream()
                .map(driverMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteDriver(String driverId) {
        log.info("Deleting driver: {}", driverId);

        if (!driverRepository.existsById(driverId)) {
            throw new ResourceNotFoundException(ErrorMessages.DRIVER_NOT_FOUND);
        }

        driverRepository.deleteById(driverId);
    }

    @Override
    public void updateAvailability(String driverId, boolean available) {
        log.info("Updating driver availability: {} -> {}", driverId, available);

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.DRIVER_NOT_FOUND));

        driver.setAvailable(available);
        driver.setLastActive(LocalDateTime.now());
        driverRepository.save(driver);
    }

    @Override
    public void updateLocation(String driverId, Double latitude, Double longitude, String location) {
        log.info("Updating driver location: {}", driverId);

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.DRIVER_NOT_FOUND));

        driver.setCurrentLatitude(latitude);
        driver.setCurrentLongitude(longitude);
        driver.setCurrentLocation(location);
        driver.setLastActive(LocalDateTime.now());
        driverRepository.save(driver);
    }

    @Override
    public List<DriverEarningDTO> getDriverEarnings(String driverId) {
        if (!driverRepository.existsById(driverId)) {
            throw new ResourceNotFoundException(ErrorMessages.DRIVER_NOT_FOUND);
        }

        List<DriverEarning> earnings = driverEarningRepository.findByDriverId(driverId);
        return earnings.stream()
                .map(driverEarningMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PaginatedResponseDTO<DriverEarningDTO> getDriverEarningsPaginated(String driverId, int page, int size) {
        if (!driverRepository.existsById(driverId)) {
            throw new ResourceNotFoundException(ErrorMessages.DRIVER_NOT_FOUND);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "earningDate"));
        Page<DriverEarning> earnings = driverEarningRepository.findByDriverId(driverId, pageable);

        List<DriverEarningDTO> content = earnings.getContent().stream()
                .map(driverEarningMapper::toDTO)
                .collect(Collectors.toList());

        return new PaginatedResponseDTO<>(content, earnings.getNumber(), earnings.getSize(), earnings.getTotalElements());
    }

    @Override
    public Double getDriverTotalEarnings(String driverId) {
        if (!driverRepository.existsById(driverId)) {
            throw new ResourceNotFoundException(ErrorMessages.DRIVER_NOT_FOUND);
        }

        Double total = driverEarningRepository.sumTotalEarnings(driverId);
        return total != null ? total : 0.0;
    }

    @Override
    public Double getDriverUnpaidEarnings(String driverId) {
        if (!driverRepository.existsById(driverId)) {
            throw new ResourceNotFoundException(ErrorMessages.DRIVER_NOT_FOUND);
        }

        Double unpaid = driverEarningRepository.sumUnpaidEarnings(driverId);
        return unpaid != null ? unpaid : 0.0;
    }

    @Override
    public void processDriverPayment(String driverId, Double amount) {
        log.info("Processing payment for driver: {}, amount: {}", driverId, amount);

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.DRIVER_NOT_FOUND));

        if (amount <= 0) {
            throw new BadRequestException("Amount must be greater than 0");
        }

        Double unpaidEarnings = getDriverUnpaidEarnings(driverId);
        if (unpaidEarnings < amount) {
            throw new BadRequestException("Insufficient unpaid earnings");
        }

        // Mark earnings as paid
        List<DriverEarning> unpaidEarningsList = driverEarningRepository.findByDriverIdAndPaidFalse(driverId);
        double remaining = amount;

        for (DriverEarning earning : unpaidEarningsList) {
            if (remaining <= 0) break;

            if (earning.getNetAmount() <= remaining) {
                earning.setPaid(true);
                earning.setPaidDate(LocalDateTime.now());
                remaining -= earning.getNetAmount();
            } else {
                // Partial payment - split the earning
                DriverEarning partialEarning = new DriverEarning();
                partialEarning.setDriver(driver);
                partialEarning.setOrder(earning.getOrder());
                partialEarning.setAmount(remaining);
                partialEarning.setCommission(0.0);
                partialEarning.setNetAmount(remaining);
                partialEarning.setCurrency(earning.getCurrency());
                partialEarning.setEarningDate(LocalDateTime.now());
                partialEarning.setPaid(true);
                partialEarning.setPaidDate(LocalDateTime.now());
                driverEarningRepository.save(partialEarning);

                earning.setAmount(earning.getAmount() - remaining);
                earning.setNetAmount(earning.getNetAmount() - remaining);
                remaining = 0.0;
                driverEarningRepository.save(earning);
            }
        }

        // Update driver balance
        driver.setAvailableBalance(driver.getAvailableBalance() - amount);
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