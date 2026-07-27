package com.inkfront.logisticsApplication.controller.admin;


import com.inkfront.logisticsApplication.dto.response.common.ApiResponseDTO;

import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import com.inkfront.logisticsApplication.dto.response.user.UserDTO;
import com.inkfront.logisticsApplication.service.interfaces.*;
import com.inkfront.logisticsApplication.domain.constants.SuccessMessages;
import io.swagger.v3.oas.annotations.Operation;
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
public class UserManagementController {


    private final UserService userService;




    @GetMapping("/users")
    @Operation(summary = "Get all users")
    public ResponseEntity<ApiResponseDTO<PaginatedResponseDTO<UserDTO>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        log.info("Get all users request");
        PaginatedResponseDTO<UserDTO> response;
        if (role != null) {
            response = userService.getUsersByRole(role, page, size);
        } else {
            response = userService.getAllUsers(page, size, sortBy, sortDirection);
        }
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @PutMapping("/users/{userId}/enable")
    @Operation(summary = "Enable user")
    public ResponseEntity<ApiResponseDTO<Void>> enableUser(@PathVariable String userId) {
        log.info("Enable user request for: {}", userId);
        userService.enableUser(userId);
        return ResponseEntity.ok(ApiResponseDTO.success("User enabled successfully", null));
    }

    @PutMapping("/users/{userId}/disable")
    @Operation(summary = "Disable user")
    public ResponseEntity<ApiResponseDTO<Void>> disableUser(@PathVariable String userId) {
        log.info("Disable user request for: {}", userId);
        userService.disableUser(userId);
        return ResponseEntity.ok(ApiResponseDTO.success("User disabled successfully", null));
    }

    @DeleteMapping("/users/{userId}")
    @Operation(summary = "Delete user")
    public ResponseEntity<ApiResponseDTO<Void>> deleteUser(@PathVariable String userId) {
        log.info("Delete user request for: {}", userId);
        userService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.USER_DELETED, null));
    }


}