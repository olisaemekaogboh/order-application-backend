package com.inkfront.logisticsApplication.mapper;

import com.inkfront.logisticsApplication.domain.entity.DriverEarning;
import com.inkfront.logisticsApplication.dto.response.driver.DriverEarningDTO;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class DriverEarningMapper {

    @Mapping(target = "driverId", source = "driver.id")

    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "orderNumber", source = "order.orderNumber")
    @Mapping(target = "formattedAmount", expression = "java(formatCurrency(earning.getAmount(), earning.getCurrency()))")
    @Mapping(target = "formattedNetAmount", expression = "java(formatCurrency(earning.getNetAmount(), earning.getCurrency()))")
    public abstract DriverEarningDTO toDTO(DriverEarning earning);

    @Mapping(target = "driver", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract DriverEarning toEntity(DriverEarningDTO driverEarningDTO);

    public abstract List<DriverEarningDTO> toDTOList(List<DriverEarning> earnings);

    protected String formatCurrency(Double amount, String currency) {
        if (amount == null) {
            return "0.00";
        }
        java.text.NumberFormat formatter = java.text.NumberFormat.getCurrencyInstance(
                java.util.Locale.US
        );
        // Customize for NGN
        if ("NGN".equals(currency)) {
            return "₦" + String.format("%,.2f", amount);
        }
        return formatter.format(amount);
    }
}