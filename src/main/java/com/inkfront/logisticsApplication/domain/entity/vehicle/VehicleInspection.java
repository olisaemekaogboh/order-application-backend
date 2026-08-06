package com.inkfront.logisticsApplication.domain.entity.vehicle;

import com.inkfront.logisticsApplication.domain.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@EqualsAndHashCode(
        callSuper = true,
        onlyExplicitlyIncluded = true
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "vehicle_inspections")
public class VehicleInspection extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(name = "inspection_date", nullable = false)
    private LocalDate inspectionDate;

    @Column(name = "inspector_name")
    private String inspectorName;

    @Column(name = "result")
    private String result; // PASS, FAIL, PENDING

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "next_inspection_date")
    private LocalDate nextInspectionDate;

    @Column(name = "certificate_number")
    private String certificateNumber;

    @Column(name = "is_compliant")
    private boolean compliant = true;
}