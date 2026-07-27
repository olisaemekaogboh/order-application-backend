package com.inkfront.logisticsApplication.controller.admin;

import com.inkfront.logisticsApplication.dto.response.common.ApiResponseDTO;

import com.inkfront.logisticsApplication.service.interfaces.*;

import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Management", description = "Admin management endpoints")
public class DriverManagementController {

    private final DriverService driverService;


    @PostMapping("/drivers/{driverId}/payments")
    public ResponseEntity<ApiResponseDTO<Void>> processDriverPayment(
            @PathVariable String driverId,
            @RequestParam Double amount) {

        log.info("Processing payment for driver {}", driverId);

        driverService.processDriverPayment(driverId, amount);

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        "Driver payment processed successfully",
                        null
                )
        );
    }
}