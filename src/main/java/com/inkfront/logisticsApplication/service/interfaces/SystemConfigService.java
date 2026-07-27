// service/interfaces/SystemConfigService.java
package com.inkfront.logisticsApplication.service.interfaces;

import com.inkfront.logisticsApplication.dto.response.admin.SystemConfigDTO;

import java.util.List;

public interface SystemConfigService {

    SystemConfigDTO getConfigById(String configId);

    SystemConfigDTO getConfigByKey(String key);

    List<SystemConfigDTO> getAllConfigs();

    List<SystemConfigDTO> getConfigsByCategory(String category);

    SystemConfigDTO createConfig(SystemConfigDTO configDTO);

    SystemConfigDTO updateConfig(String key, String value);

    SystemConfigDTO updateConfig(String key, String value, String updatedBy);

    void deleteConfig(String configId);

    void deleteConfigByKey(String key);

    String getConfigValue(String key);

    <T> T getConfigValue(String key, Class<T> targetType);

    void loadAllConfigsIntoCache();

    void refreshCache();
}