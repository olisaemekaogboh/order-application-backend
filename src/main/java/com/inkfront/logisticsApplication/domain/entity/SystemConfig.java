package com.inkfront.logisticsApplication.domain.entity;



import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "system_configs")
public class SystemConfig extends BaseEntity {

    @Column(name = "config_key", nullable = false, unique = true)
    private String configKey;

    @Column(name = "config_value", nullable = false)
    private String configValue;

    @Column(name = "description")
    private String description;

    @Column(name = "category")
    private String category;

    @Column(name = "is_encrypted")
    private boolean encrypted = false;

    @Column(name = "is_public")
    private boolean publicAccess = false;

    @Column(name = "validation_rules")
    private String validationRules;

    @Column(name = "updated_by")
    private String updatedBy;
}
