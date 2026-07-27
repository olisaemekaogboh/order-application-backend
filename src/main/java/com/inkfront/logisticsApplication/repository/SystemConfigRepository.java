package com.inkfront.logisticsApplication.repository;

import com.inkfront.logisticsApplication.domain.entity.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, String> {

    Optional<SystemConfig> findByConfigKey(String configKey);

    List<SystemConfig> findByCategory(String category);

    List<SystemConfig> findByPublicAccessTrue();

    boolean existsByConfigKey(String configKey);

    @Query("SELECT sc FROM SystemConfig sc WHERE sc.category = :category AND sc.publicAccess = true")
    List<SystemConfig> findPublicConfigsByCategory(@Param("category") String category);

    @Modifying
    @Query("UPDATE SystemConfig sc SET sc.configValue = :value, sc.updatedBy = :updatedBy WHERE sc.configKey = :key")
    void updateConfigValue(
            @Param("key") String key,
            @Param("value") String value,
            @Param("updatedBy") String updatedBy
    );
}