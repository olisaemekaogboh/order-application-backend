package com.inkfront.logisticsApplication.domain.entity.dispatch;

import com.inkfront.logisticsApplication.domain.entity.BaseEntity;
import com.inkfront.logisticsApplication.domain.enums.DispatchStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@EqualsAndHashCode(
        callSuper = true,
        onlyExplicitlyIncluded = true
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "dispatch_histories")
public class DispatchHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispatch_id", nullable = false)
    private Dispatch dispatch;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status")
    private DispatchStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false)
    private DispatchStatus newStatus;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(name = "changed_by")
    private String changedBy;

    @Column(name = "reason")
    private String reason;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;


}