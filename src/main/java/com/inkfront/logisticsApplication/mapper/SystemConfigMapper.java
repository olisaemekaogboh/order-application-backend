package com.inkfront.logisticsApplication.mapper;

import com.inkfront.logisticsApplication.domain.entity.SystemConfig;
import com.inkfront.logisticsApplication.dto.request.admin.SystemConfigRequestDTO;
import com.inkfront.logisticsApplication.dto.request.admin.SystemConfigUpdateRequestDTO;
import com.inkfront.logisticsApplication.dto.response.admin.SystemConfigDTO;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SystemConfigMapper {

    SystemConfigDTO toDTO(SystemConfig entity);

    SystemConfig toEntity(SystemConfigRequestDTO request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(
            SystemConfigUpdateRequestDTO request,
            @MappingTarget SystemConfig entity
    );
}