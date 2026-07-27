package com.inkfront.logisticsApplication.service.interfaces;

import com.inkfront.logisticsApplication.dto.request.user.UserUpdateRequestDTO;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import com.inkfront.logisticsApplication.dto.response.user.UserDTO;

import java.util.List;

public interface UserService {

    UserDTO getUserById(String userId);

    UserDTO getUserByEmail(String email);

    UserDTO updateUser(String userId, UserUpdateRequestDTO updateRequest);

    void deleteUser(String userId);

    void enableUser(String userId);

    void disableUser(String userId);

    PaginatedResponseDTO<UserDTO> getAllUsers(int page, int size, String sortBy, String sortDirection);

    PaginatedResponseDTO<UserDTO> getUsersByRole(String role, int page, int size);

    List<UserDTO> getRecentUsers(int limit);

    long countTotalUsers();

    long countUsersByRole(String role);

    long countNewUsersToday();

    void changePassword(String userId, String oldPassword, String newPassword);

    void updateProfilePicture(String userId, String profilePictureUrl);
}