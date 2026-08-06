package com.inkfront.logisticsApplication.mapper;

import com.inkfront.logisticsApplication.domain.entity.Order;
import com.inkfront.logisticsApplication.dto.request.order.OrderRequestDTO;
import com.inkfront.logisticsApplication.dto.request.order.OrderUpdateRequestDTO;
import com.inkfront.logisticsApplication.dto.response.order.OrderResponseDTO;
import com.inkfront.logisticsApplication.dto.response.order.OrderTrackingDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class OrderMapper {

    @Autowired
    protected UserMapper userMapper;

    @Autowired
    protected DriverMapper driverMapper;

    // ======================== ORDER TO DTO ========================

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "driver", ignore = true)
    @Mapping(target = "driverName",
            expression = "java(order.getDriver() != null ? order.getDriver().getUser().getFullName() : null)")
    @Mapping(target = "driverPhone",
            expression = "java(order.getDriver() != null ? order.getDriver().getUser().getPhoneNumber() : null)")
    @Mapping(target = "statusDisplayName",
            expression = "java(order.getStatus().getDisplayName())")
    @Mapping(target = "isDelivered",
            expression = "java(order.isDelivered())")
    @Mapping(target = "isCancelled",
            expression = "java(order.isCancelled())")
    @Mapping(target = "isPending",
            expression = "java(order.isPending())")
    @Mapping(target = "isPaid",
            expression = "java(order.isPaid())")
    @Mapping(target = "estimatedDeliveryTime",
            expression = "java(calculateEstimatedDeliveryTime(order))")
    public abstract OrderResponseDTO toDTO(Order order);

    // ======================== DTO TO ENTITY ========================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderNumber", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "paymentStatus", constant = "PENDING")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "driver", ignore = true)
    @Mapping(target = "driverEarning", ignore = true)
    @Mapping(target = "paymentTransaction", ignore = true)
    @Mapping(target = "deliveryAddress", ignore = true)
    public abstract Order toEntity(OrderRequestDTO orderRequestDTO);

    // ======================== UPDATE ORDER ========================

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderNumber", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "driver", ignore = true)
    @Mapping(target = "driverEarning", ignore = true)
    @Mapping(target = "paymentTransaction", ignore = true)
    @Mapping(target = "deliveryAddress", ignore = true)
    public abstract void updateOrderFromDTO(
            OrderUpdateRequestDTO dto,
            @MappingTarget Order order
    );

    // ======================== ORDER TO TRACKING DTO ========================
    // Matches the actual OrderTrackingDTO structure

    @Mapping(target = "orderNumber", source = "orderNumber")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "statusDisplayName",
            expression = "java(order.getStatus() != null ? order.getStatus().getDisplayName() : null)")
    @Mapping(target = "pickupLocation", source = "pickupLocation")
    @Mapping(target = "deliveryLocation", source = "deliveryLocation")
    @Mapping(target = "estimatedDeliveryDate", source = "estimatedDeliveryDate")

    // Driver info - from driver entity
    @Mapping(target = "driverName",
            expression = "java(order.getDriver() != null ? order.getDriver().getUser().getFullName() : null)")
    @Mapping(target = "driverPhone",
            expression = "java(order.getDriver() != null ? order.getDriver().getUser().getPhoneNumber() : null)")
    @Mapping(target = "driverPhoto", ignore = true)

    // Location info - from driver entity
    @Mapping(target = "currentLatitude",
            expression = "java(order.getDriver() != null ? order.getDriver().getCurrentLatitude() : null)")
    @Mapping(target = "currentLongitude",
            expression = "java(order.getDriver() != null ? order.getDriver().getCurrentLongitude() : null)")

    // Tracking history - to be populated separately
    @Mapping(target = "trackingHistory", ignore = true)

    public abstract OrderTrackingDTO toTrackingDTO(Order order);

    // ======================== BATCH CONVERSION ========================

    public abstract List<OrderResponseDTO> toDTOList(List<Order> orders);

    // ======================== AFTER MAPPING ========================

    @AfterMapping
    protected void mapUserAndDriver(
            @MappingTarget OrderResponseDTO dto,
            Order order
    ) {
        if (order.getUser() != null) {
            dto.setUser(userMapper.toDTO(order.getUser()));
        }

        if (order.getDriver() != null) {
            dto.setDriver(driverMapper.toDTO(order.getDriver()));
        }
    }

    // ======================== HELPER METHODS ========================

    protected String calculateEstimatedDeliveryTime(Order order) {
        if (order.getEstimatedDeliveryDate() == null) {
            return null;
        }

        Duration duration = Duration.between(
                LocalDateTime.now(),
                order.getEstimatedDeliveryDate()
        );

        long hours = duration.toHours();

        if (hours < 1) {
            return "Less than an hour";
        }

        if (hours < 24) {
            return hours + " hours";
        }

        long days = hours / 24;
        return days + " days";
    }
}