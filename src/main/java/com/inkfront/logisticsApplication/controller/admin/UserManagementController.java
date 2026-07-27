package com.inkfront.logisticsApplication.controller.admin;

import com.inkfront.logisticsApplication.dto.request.user.UserStatusUpdateRequestDTO;
import com.inkfront.logisticsApplication.dto.response.common.ApiResponseDTO;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import com.inkfront.logisticsApplication.dto.response.user.UserDTO;
import com.inkfront.logisticsApplication.service.interfaces.UserService;
import com.inkfront.logisticsApplication.domain.constants.SuccessMessages;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

    @PutMapping("/users/{userId}/status")
    @Operation(summary = "Update user status (enable/disable)")
    public ResponseEntity<ApiResponseDTO<UserDTO>> updateUserStatus(
            @PathVariable String userId,
            @Valid @RequestBody UserStatusUpdateRequestDTO request) {
        log.info("Update user status request for user: {} to enabled={}", userId, request.getEnabled());
        UserDTO response = userService.updateUserStatus(userId, request);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.USER_UPDATED, response));
    }

    @DeleteMapping("/users/{userId}")
    @Operation(summary = "Delete user")
    public ResponseEntity<ApiResponseDTO<Void>> deleteUser(@PathVariable String userId) {
        log.info("Delete user request for: {}", userId);
        userService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.USER_DELETED, null));
    }
}