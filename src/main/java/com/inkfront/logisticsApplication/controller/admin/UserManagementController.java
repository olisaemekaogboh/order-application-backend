package com.inkfront.logisticsApplication.controller.admin;

import com.inkfront.logisticsApplication.dto.request.user.UserStatusUpdateRequestDTO;
import com.inkfront.logisticsApplication.dto.response.common.ApiResponseDTO;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import com.inkfront.logisticsApplication.dto.response.user.UserDTO;
import com.inkfront.logisticsApplication.dto.response.user.UserStatsDTO;
import com.inkfront.logisticsApplication.service.interfaces.UserService;
import com.inkfront.logisticsApplication.domain.constants.SuccessMessages;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin Management", description = "Admin management endpoints")
public class UserManagementController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Get all users")
    public ResponseEntity<ApiResponseDTO<PaginatedResponseDTO<UserDTO>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        log.info("Get all users request with search: {}", search);
        PaginatedResponseDTO<UserDTO> response;
        if (role != null) {
            response = userService.getUsersByRole(role, page, size);
        } else if (search != null && !search.isEmpty()) {
            response = userService.searchUsers(search, page, size);
        } else {
            response = userService.getAllUsers(page, size, sortBy, sortDirection);
        }
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<ApiResponseDTO<UserDTO>> getUserById(@PathVariable String userId) {
        log.info("Get user by ID request: {}", userId);
        UserDTO response = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @PutMapping("/{userId}/status")
    @Operation(summary = "Update user status (enable/disable)")
    public ResponseEntity<ApiResponseDTO<UserDTO>> updateUserStatus(
            @PathVariable String userId,
            @Valid @RequestBody UserStatusUpdateRequestDTO request) {
        log.info("Update user status request for user: {} to enabled={}", userId, request.getEnabled());
        UserDTO response = userService.updateUserStatus(userId, request);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.USER_UPDATED, response));
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete user")
    public ResponseEntity<ApiResponseDTO<Void>> deleteUser(@PathVariable String userId) {
        log.info("Delete user request for: {}", userId);
        userService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.USER_DELETED, null));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get user statistics")
    public ResponseEntity<ApiResponseDTO<UserStatsDTO>> getUserStats() {
        log.info("Get user statistics request");
        UserStatsDTO stats = userService.getUserStats();
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, stats));
    }

    @GetMapping("/export")
    @Operation(summary = "Export users")
    public ResponseEntity<org.springframework.core.io.Resource> exportUsers(
            @RequestParam(defaultValue = "csv") String format) {
        log.info("Export users request with format: {}", format);
        org.springframework.core.io.Resource resource = userService.exportUsers(format);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/" + format))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"users." + format + "\"")
                .body(resource);
    }
}