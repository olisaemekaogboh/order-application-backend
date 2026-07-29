package com.inkfront.logisticsApplication.domain.entity.vehicle;

import com.inkfront.logisticsApplication.domain.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "vehicle_documents")
public class VehicleDocument extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(name = "document_type", nullable = false)
    private String documentType; // INSURANCE, REGISTRATION, etc.

    @Column(name = "document_number")
    private String documentNumber;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "is_valid")
    private boolean valid = true;

    @Column(name = "notes")
    private String notes;
}