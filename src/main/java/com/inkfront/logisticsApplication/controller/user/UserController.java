package com.inkfront.logisticsApplication.controller.user;

import com.inkfront.logisticsApplication.domain.constants.SuccessMessages;
import com.inkfront.logisticsApplication.dto.request.user.AddressRequestDTO;
import com.inkfront.logisticsApplication.dto.request.user.UserUpdateRequestDTO;
import com.inkfront.logisticsApplication.dto.response.common.ApiResponseDTO;
import com.inkfront.logisticsApplication.dto.response.user.AddressDTO;
import com.inkfront.logisticsApplication.dto.response.user.UserDTO;
import com.inkfront.logisticsApplication.security.AuthenticatedUser;
import com.inkfront.logisticsApplication.service.interfaces.AddressService;
import com.inkfront.logisticsApplication.service.interfaces.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User management endpoints")
public class UserController {

    private final UserService userService;
    private final AddressService addressService;

    @GetMapping("/profile")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<ApiResponseDTO<UserDTO>> getProfile(
            Authentication authentication) {

        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        log.info("Get profile request for user: {}", user.getEmail());

        UserDTO response = userService.getUserById(user.getId());

        return ResponseEntity.ok(
                ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response)
        );
    }

    @PutMapping("/profile")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<ApiResponseDTO<UserDTO>> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UserUpdateRequestDTO updateRequest) {

        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        log.info("Update profile request for user: {}", user.getEmail());

        UserDTO response = userService.updateUser(user.getId(), updateRequest);

        return ResponseEntity.ok(
                ApiResponseDTO.success(SuccessMessages.PROFILE_UPDATED, response)
        );
    }

    @PostMapping("/profile/picture")
    @Operation(summary = "Update profile picture")
    public ResponseEntity<ApiResponseDTO<Void>> updateProfilePicture(
            Authentication authentication,
            @RequestParam String pictureUrl) {

        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        log.info("Update profile picture request for user: {}", user.getEmail());

        userService.updateProfilePicture(user.getId(), pictureUrl);

        return ResponseEntity.ok(
                ApiResponseDTO.success("Profile picture updated successfully", null)
        );
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password")
    public ResponseEntity<ApiResponseDTO<Void>> changePassword(
            Authentication authentication,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {

        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        log.info("Change password request for user: {}", user.getEmail());

        userService.changePassword(user.getId(), oldPassword, newPassword);

        return ResponseEntity.ok(
                ApiResponseDTO.success("Password changed successfully", null)
        );
    }

    @GetMapping("/addresses")
    @Operation(summary = "Get user addresses")
    public ResponseEntity<ApiResponseDTO<List<AddressDTO>>> getAddresses(
            Authentication authentication) {

        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        log.info("Get addresses request for user: {}", user.getEmail());

        List<AddressDTO> response = addressService.getUserAddresses(user.getId());

        return ResponseEntity.ok(
                ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response)
        );
    }

    @PostMapping("/addresses")
    @Operation(summary = "Add new address")
    public ResponseEntity<ApiResponseDTO<AddressDTO>> addAddress(
            Authentication authentication,
            @Valid @RequestBody AddressRequestDTO addressRequest) {

        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        log.info("Add address request for user: {}", user.getEmail());

        AddressDTO response =
                addressService.createAddress(user.getId(), addressRequest);

        return ResponseEntity.ok(
                ApiResponseDTO.success("Address added successfully", response)
        );
    }

    @PutMapping("/addresses/{addressId}")
    @Operation(summary = "Update address")
    public ResponseEntity<ApiResponseDTO<AddressDTO>> updateAddress(
            Authentication authentication,
            @PathVariable String addressId,
            @Valid @RequestBody AddressRequestDTO addressRequest) {

        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        log.info("Update address request for user: {} address: {}",
                user.getEmail(), addressId);

        AddressDTO response = addressService.updateAddress(
                user.getId(),
                addressId,
                addressRequest
        );

        return ResponseEntity.ok(
                ApiResponseDTO.success("Address updated successfully", response)
        );
    }

    @DeleteMapping("/addresses/{addressId}")
    @Operation(summary = "Delete address")
    public ResponseEntity<ApiResponseDTO<Void>> deleteAddress(
            Authentication authentication,
            @PathVariable String addressId) {

        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        log.info("Delete address request for user: {} address: {}",
                user.getEmail(), addressId);

        addressService.deleteAddress(
                user.getId(),
                addressId
        );

        return ResponseEntity.ok(
                ApiResponseDTO.success("Address deleted successfully", null)
        );
    }

    @PutMapping("/addresses/default/{addressId}")
    @Operation(summary = "Set default address")
    public ResponseEntity<ApiResponseDTO<Void>> setDefaultAddress(
            Authentication authentication,
            @PathVariable String addressId) {

        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        log.info("Set default address request for user: {} address: {}",
                user.getEmail(), addressId);

        addressService.setDefaultAddress(user.getId(), addressId);

        return ResponseEntity.ok(
                ApiResponseDTO.success("Default address set successfully", null)
        );
    }

    @GetMapping("/addresses/default")
    @Operation(summary = "Get default address")
    public ResponseEntity<ApiResponseDTO<AddressDTO>> getDefaultAddress(
            Authentication authentication) {

        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        log.info("Get default address request for user: {}", user.getEmail());

        AddressDTO response =
                addressService.getDefaultAddress(user.getId());

        return ResponseEntity.ok(
                ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response)
        );
    }

    @GetMapping("/addresses/{addressId}")
    @Operation(summary = "Get address by ID")
    public ResponseEntity<ApiResponseDTO<AddressDTO>> getAddressById(
            Authentication authentication,
            @PathVariable String addressId) {

        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        log.info("Get address request for user: {} address: {}",
                user.getEmail(), addressId);

        AddressDTO response = addressService.getAddressById(
                user.getId(),
                addressId
        );

        return ResponseEntity.ok(
                ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response)
        );
    }

    @PostMapping("/addresses/geocode")
    @Operation(summary = "Geocode address")
    public ResponseEntity<ApiResponseDTO<AddressDTO>> geocodeAddress(
            @RequestParam String address) {

        log.info("Geocode address request for: {}", address);

        AddressDTO response = addressService.geocodeAddress(address);

        return ResponseEntity.ok(
                ApiResponseDTO.success("Address geocoded successfully", response)
        );
    }
}