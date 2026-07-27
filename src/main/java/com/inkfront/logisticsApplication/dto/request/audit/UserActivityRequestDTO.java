package com.inkfront.logisticsApplication.dto.request.audit;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserActivityRequestDTO {

    @NotBlank(message = "User ID is required")
    private String userId;

    private LocalDate startDate;
    private LocalDate endDate;
}