// service/impl/SystemConfigServiceImpl.java
package com.inkfront.logisticsApplication.service.impl;

import com.inkfront.logisticsApplication.domain.entity.SystemConfig;
import com.inkfront.logisticsApplication.dto.request.admin.SystemConfigRequestDTO;
import com.inkfront.logisticsApplication.dto.request.admin.SystemConfigUpdateRequestDTO;
import com.inkfront.logisticsApplication.dto.response.admin.SystemConfigDTO;
import com.inkfront.logisticsApplication.exception.ResourceNotFoundException;
import com.inkfront.logisticsApplication.mapper.SystemConfigMapper;
import com.inkfront.logisticsApplication.repository.SystemConfigRepository;
import com.inkfront.logisticsApplication.service.interfaces.SystemConfigService;
import com.inkfront.logisticsApplication.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SystemConfigServiceImpl implements SystemConfigService {

    private final SystemConfigRepository systemConfigRepository;
    private final SystemConfigMapper systemConfigMapper;

    @Override
    public SystemConfigDTO getConfigById(String configId) {
        SystemConfig config = systemConfigRepository.findById(configId)
                .orElseThrow(() -> new ResourceNotFoundException("Config not found with id: " + configId));
        return systemConfigMapper.toDTO(config);
    }

    @Override
    @Cacheable(value = "systemConfigs", key = "#key")
    public SystemConfigDTO getConfigByKey(String key) {
        SystemConfig config = systemConfigRepository.findByConfigKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("Config not found with key: " + key));
        return systemConfigMapper.toDTO(config);
    }

    @Override
    public List<SystemConfigDTO> getAllConfigs() {
        return systemConfigRepository.findAll().stream()
                .map(systemConfigMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SystemConfigDTO> getConfigsByCategory(String category) {
        return systemConfigRepository.findByCategory(category).stream()
                .map(systemConfigMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SystemConfigDTO createConfig(SystemConfigRequestDTO request) {
        SystemConfig config = systemConfigMapper.toEntity(request);
        config = systemConfigRepository.save(config);
        return systemConfigMapper.toDTO(config);
    }

    @Override
    @CacheEvict(value = "systemConfigs", key = "#key")
    public SystemConfigDTO updateConfig(String key,    SystemConfigUpdateRequestDTO request) {
        return updateConfig(key, request, SecurityUtils.getCurrentUsername());
    }

    @Override
    @CacheEvict(value = "systemConfigs", key = "#key")
    public SystemConfigDTO updateConfig(String key,SystemConfigUpdateRequestDTO request , String updatedBy) {
        SystemConfig config = systemConfigRepository.findByConfigKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("Config not found with key: " + key));

        config.setConfigValue(request.getConfigValue());
        config.setUpdatedBy(updatedBy);
        config = systemConfigRepository.save(config);

        return systemConfigMapper.toDTO(config);
    }

    @Override
    public void deleteConfig(String configId) {
        if (!systemConfigRepository.existsById(configId)) {
            throw new ResourceNotFoundException("Config not found with id: " + configId);
        }
        systemConfigRepository.deleteById(configId);
    }

    @Override
    @CacheEvict(value = "systemConfigs", key = "#key")
    public void deleteConfigByKey(String key) {
        SystemConfig config = systemConfigRepository.findByConfigKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("Config not found with key: " + key));
        systemConfigRepository.delete(config);
    }

    @Override
    @Cacheable(value = "systemConfigs", key = "#key")
    public String getConfigValue(String key) {
        return systemConfigRepository.findByConfigKey(key)
                .map(SystemConfig::getConfigValue)
                .orElse(null);
    }

    @Override
    @Cacheable(value = "systemConfigs", key = "#key + '_' + #targetType.getName()")
    public <T> T getConfigValue(String key, Class<T> targetType) {
        String value = getConfigValue(key);
        if (value == null) {
            return null;
        }
        // Simple type conversion
        if (targetType == String.class) {
            return targetType.cast(value);
        } else if (targetType == Integer.class) {
            return targetType.cast(Integer.valueOf(value));
        } else if (targetType == Long.class) {
            return targetType.cast(Long.valueOf(value));
        } else if (targetType == Double.class) {
            return targetType.cast(Double.valueOf(value));
        } else if (targetType == Boolean.class) {
            return targetType.cast(Boolean.valueOf(value));
        }
        throw new IllegalArgumentException("Unsupported type: " + targetType.getName());
    }

    @Override
    public void loadAllConfigsIntoCache() {
        log.info("Loading all system configurations into cache");
        List<SystemConfig> configs = systemConfigRepository.findAll();
        configs.forEach(config -> {
            // This will trigger caching
            getConfigValue(config.getConfigKey());
        });
    }

    @Override
    @CacheEvict(value = "systemConfigs", allEntries = true)
    public void refreshCache() {
        log.info("Refreshing system configurations cache");
        loadAllConfigsIntoCache();
    }
}