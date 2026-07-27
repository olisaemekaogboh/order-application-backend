package com.inkfront.logisticsApplication.mapper;

import com.inkfront.logisticsApplication.domain.entity.Driver;
import com.inkfront.logisticsApplication.domain.entity.DriverEarning;
import com.inkfront.logisticsApplication.dto.request.driver.DriverRegistrationRequestDTO;
import com.inkfront.logisticsApplication.dto.request.driver.DriverUpdateRequestDTO;
import com.inkfront.logisticsApplication.dto.response.driver.DriverDTO;
import com.inkfront.logisticsApplication.dto.response.driver.DriverEarningDTO;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class DriverMapper {

    @Autowired
    protected DriverEarningMapper driverEarningMapper;

    @Mapping(
            target = "vehicleTypeDisplay",
            expression = "java(driver.getVehicleType() != null ? driver.getVehicleType().getDisplayName() : null)"
    )
    @Mapping(
            target = "activeOrders",
            expression = "java(countActiveOrders(driver))"
    )
    @Mapping(
            target = "averageRating",
            expression = "java(driver.getRating() != null ? driver.getRating() : 0.0)"
    )
    @Mapping(
            target = "formattedRating",
            expression = "java(formatRating(driver.getRating()))"
    )
    @Mapping(
            target = "recentEarnings",
            expression = "java(mapEarningsToDTO(driver.getEarnings()))"
    )
    public abstract DriverDTO toDTO(Driver driver);

    public abstract List<DriverDTO> toDTOList(List<Driver> drivers);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "availableBalance", constant = "0.0")
    @Mapping(target = "rating", constant = "0.0")
    @Mapping(target = "totalDeliveries", constant = "0")
    @Mapping(target = "available", constant = "true")
    @Mapping(target = "verified", constant = "false")
    @Mapping(target = "totalEarnings", constant = "0.0")
    @Mapping(target = "completedOrders", constant = "0")
    @Mapping(target = "currentLocation", ignore = true)
    @Mapping(target = "currentLatitude", ignore = true)
    @Mapping(target = "currentLongitude", ignore = true)
    @Mapping(target = "lastActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "assignedOrders", ignore = true)
    @Mapping(target = "earnings", ignore = true)
    public abstract Driver toEntity(DriverRegistrationRequestDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "availableBalance", ignore = true)
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "totalDeliveries", ignore = true)
    @Mapping(target = "verified", ignore = true)
    @Mapping(target = "totalEarnings", ignore = true)
    @Mapping(target = "completedOrders", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "assignedOrders", ignore = true)
    @Mapping(target = "earnings", ignore = true)
    public abstract void updateDriverFromDTO(
            DriverUpdateRequestDTO dto,
            @MappingTarget Driver driver
    );

    protected List<DriverEarningDTO> mapEarningsToDTO(List<DriverEarning> earnings) {

        if (earnings == null || earnings.isEmpty()) {
            return Collections.emptyList();
        }

        return earnings.stream()
                .map(driverEarningMapper::toDTO)
                .collect(Collectors.toList());
    }

    protected Integer countActiveOrders(Driver driver) {

        if (driver == null || driver.getAssignedOrders() == null) {
            return 0;
        }

        return (int) driver.getAssignedOrders()
                .stream()
                .filter(order -> !order.isDelivered() && !order.isCancelled())
                .count();
    }

    protected String formatRating(Double rating) {

        if (rating == null) {
            return "0.0";
        }

        return String.format("%.1f", rating);
    }
}