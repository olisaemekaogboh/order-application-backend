// mapper/PricingConfigMapper.java
package com.inkfront.logisticsApplication.mapper;

import com.inkfront.logisticsApplication.domain.entity.PricingConfig;
import com.inkfront.logisticsApplication.dto.request.admin.PricingConfigRequestDTO;
import com.inkfront.logisticsApplication.dto.response.admin.PricingConfigDTO;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PricingConfigMapper {

    @Mapping(target = "vehicleTypeDisplay", expression = "java(pricingConfig.getVehicleType().getDisplayName())")
    PricingConfigDTO toDTO(PricingConfig pricingConfig);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    PricingConfig toEntity(PricingConfigRequestDTO request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updatePricingConfigFromDTO(PricingConfigRequestDTO dto, @MappingTarget PricingConfig config);

    List<PricingConfigDTO> toDTOList(List<PricingConfig> configs);
}