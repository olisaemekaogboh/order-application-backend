package com.inkfront.logisticsApplication.service.interfaces;

import com.inkfront.logisticsApplication.dto.request.user.ChangePasswordRequestDTO;
import com.inkfront.logisticsApplication.dto.request.user.UpdateProfileRequestDTO;
import com.inkfront.logisticsApplication.dto.request.user.UserRoleUpdateRequestDTO;
import com.inkfront.logisticsApplication.dto.request.user.UserStatusUpdateRequestDTO;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import com.inkfront.logisticsApplication.dto.response.user.UserDTO;

import java.util.List;

public interface UserService {

    UserDTO getUserById(String userId);

    UserDTO getUserByEmail(String email);

    // Updated profile method using DTO
    UserDTO updateProfile(String userId, UpdateProfileRequestDTO request);

    void deleteUser(String userId);

    // Combined status update (replaces enableUser/disableUser)
    UserDTO updateUserStatus(String userId, UserStatusUpdateRequestDTO request);

    // New role update method
    UserDTO updateUserRole(String userId, UserRoleUpdateRequestDTO request);

    PaginatedResponseDTO<UserDTO> getAllUsers(int page, int size, String sortBy, String sortDirection);

    PaginatedResponseDTO<UserDTO> getUsersByRole(String role, int page, int size);

    List<UserDTO> getRecentUsers(int limit);

    long countTotalUsers();

    long countUsersByRole(String role);

    long countNewUsersToday();

    // Updated password change method returning UserDTO
    UserDTO changePassword(String userId, ChangePasswordRequestDTO request);

    void updateProfilePicture(String userId, String profilePictureUrl);
}