package com.inkfront.logisticsApplication.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inkfront.logisticsApplication.domain.entity.PaymentTransaction;
import com.inkfront.logisticsApplication.dto.response.payment.PaymentResponseDTO;
import com.inkfront.logisticsApplication.dto.response.payment.PaymentSummaryDTO;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Mapper(componentModel = "spring")
public abstract class PaymentMapper {

    @Autowired
    protected ObjectMapper objectMapper;

    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "metadata", source = "metadata", qualifiedByName = "jsonToMap")
    public abstract PaymentResponseDTO toResponseDTO(PaymentTransaction transaction);

    @Mapping(target = "orderId", source = "order.id")
    public abstract PaymentSummaryDTO toSummaryDTO(PaymentTransaction transaction);

    public abstract List<PaymentSummaryDTO> toSummaryDTOList(List<PaymentTransaction> transactions);

    @Named("jsonToMap")
    protected Map<String, Object> jsonToMap(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyMap();
        }

        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception ex) {
            log.error("Failed to deserialize JSON to Map", ex);
            return Collections.emptyMap();
        }
    }

    @Named("bigDecimalToDouble")
    protected Double bigDecimalToDouble(BigDecimal value) {
        return value != null ? value.doubleValue() : null;
    }
}