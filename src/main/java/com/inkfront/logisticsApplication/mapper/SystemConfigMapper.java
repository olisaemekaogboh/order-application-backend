package com.inkfront.logisticsApplication.mapper;

import com.inkfront.logisticsApplication.domain.entity.SystemConfig;
import com.inkfront.logisticsApplication.dto.response.admin.SystemConfigDTO;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class SystemConfigMapper {

    public abstract SystemConfigDTO toDTO(SystemConfig systemConfig);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract SystemConfig toEntity(SystemConfigDTO systemConfigDTO);

    public abstract List<SystemConfigDTO> toDTOList(List<SystemConfig> systemConfigs);
}