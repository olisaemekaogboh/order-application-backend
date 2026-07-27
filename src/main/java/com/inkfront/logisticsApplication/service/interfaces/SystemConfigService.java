package com.inkfront.logisticsApplication.service.interfaces;

import com.inkfront.logisticsApplication.dto.request.admin.SystemConfigRequestDTO;
import com.inkfront.logisticsApplication.dto.request.admin.SystemConfigUpdateRequestDTO;
import com.inkfront.logisticsApplication.dto.response.admin.SystemConfigDTO;

import java.util.List;

public interface SystemConfigService {

    SystemConfigDTO getConfigById(String configId);

    SystemConfigDTO getConfigByKey(String key);

    List<SystemConfigDTO> getAllConfigs();

    List<SystemConfigDTO> getConfigsByCategory(String category);

    SystemConfigDTO createConfig(SystemConfigRequestDTO request);

    SystemConfigDTO updateConfig(
            String key,
            SystemConfigUpdateRequestDTO request
    );

    SystemConfigDTO updateConfig(
            String key,
            SystemConfigUpdateRequestDTO request,
            String updatedBy
    );

    void deleteConfig(String configId);

    void deleteConfigByKey(String key);

    String getConfigValue(String key);

    <T> T getConfigValue(String key, Class<T> targetType);

    void loadAllConfigsIntoCache();

    void refreshCache();
}