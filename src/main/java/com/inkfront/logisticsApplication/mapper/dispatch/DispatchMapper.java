package com.inkfront.logisticsApplication.mapper.dispatch;

import com.inkfront.logisticsApplication.domain.entity.dispatch.Dispatch;
import com.inkfront.logisticsApplication.dto.response.dispatch.DispatchResponseDTO;
import com.inkfront.logisticsApplication.dto.response.dispatch.DispatchSummaryDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DispatchMapper {

    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "orderNumber", source = "order.orderNumber")
    @Mapping(target = "driverId", source = "driver.id")
    @Mapping(target = "driverName", source = "driver.name")
    @Mapping(target = "vehicleId", source = "vehicle.id")
    @Mapping(target = "vehicleNumber", source = "vehicle.vehicleNumber")
    DispatchResponseDTO toResponseDTO(Dispatch dispatch);

    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "orderNumber", source = "order.orderNumber")
    @Mapping(target = "pickupLocation", source = "order.pickupLocation")
    @Mapping(target = "deliveryLocation", source = "order.deliveryLocation")

    @Mapping(target = "driverId", source = "driver.id")
    @Mapping(target = "driverName", source = "driver.name")

    @Mapping(target = "vehicleId", source = "vehicle.id")
    @Mapping(target = "vehicleNumber", source = "vehicle.vehicleNumber")

    @Mapping(target = "priority", source = "priority")
    @Mapping(target = "retryCount", source = "retryCount")

    @Mapping(target = "assignedAt", source = "assignedAt")
    @Mapping(target = "acceptedAt", source = "acceptedAt")
    @Mapping(target = "completedAt", source = "completedAt")

    DispatchSummaryDTO toSummaryDTO(Dispatch dispatch);
    List<DispatchSummaryDTO> toSummaryDTOList(List<Dispatch> dispatches);
}