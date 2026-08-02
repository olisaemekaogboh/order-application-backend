package com.inkfront.logisticsApplication.service.impl;

import com.inkfront.logisticsApplication.domain.entity.User;
import com.inkfront.logisticsApplication.domain.enums.UserRole;
import com.inkfront.logisticsApplication.dto.request.user.ChangePasswordRequestDTO;
import com.inkfront.logisticsApplication.dto.request.user.UpdateProfileRequestDTO;
import com.inkfront.logisticsApplication.dto.request.user.UserRoleUpdateRequestDTO;
import com.inkfront.logisticsApplication.dto.request.user.UserStatusUpdateRequestDTO;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import com.inkfront.logisticsApplication.dto.response.user.UserDTO;
import com.inkfront.logisticsApplication.dto.response.user.UserStatsDTO;
import com.inkfront.logisticsApplication.exception.BadRequestException;
import com.inkfront.logisticsApplication.exception.ResourceNotFoundException;
import com.inkfront.logisticsApplication.mapper.UserMapper;
import com.inkfront.logisticsApplication.repository.UserRepository;
import com.inkfront.logisticsApplication.service.interfaces.UserService;
import com.inkfront.logisticsApplication.domain.constants.ErrorMessages;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDTO getUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));
        return userMapper.toDTO(user);
    }

    @Override
    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));
        return userMapper.toDTO(user);
    }

    @Override
    public UserDTO updateProfile(String userId, UpdateProfileRequestDTO request) {
        log.info("Updating profile for user {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

        // Check if email is being changed and is available
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BadRequestException(ErrorMessages.EMAIL_ALREADY_EXISTS);
            }
        }

        // Check if phone is being changed and is available
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().equals(user.getPhoneNumber())) {
            if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                throw new BadRequestException(ErrorMessages.PHONE_ALREADY_EXISTS);
            }
        }

        // Update fields
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        user.setPhoneNumber(request.getPhoneNumber());
        if (request.getProfileImage() != null) {
            user.setProfilePicture(request.getProfileImage());
        }

        user = userRepository.save(user);

        return userMapper.toDTO(user);
    }

    @Override
    public void deleteUser(String userId) {
        log.info("Deleting user: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND);
        }

        userRepository.deleteById(userId);
    }

    @Override
    public UserDTO updateUserStatus(String userId, UserStatusUpdateRequestDTO request) {
        log.info("Updating status for user {} to enabled={}", userId, request.getEnabled());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

        user.setEnabled(request.getEnabled());
        user = userRepository.save(user);

        return userMapper.toDTO(user);
    }

    @Override
    public UserDTO updateUserRole(String userId, UserRoleUpdateRequestDTO request) {
        log.info("Updating role for user {} to {}", userId, request.getRole());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

        user.setRole(request.getRole());
        user = userRepository.save(user);

        return userMapper.toDTO(user);
    }

    @Override
    public PaginatedResponseDTO<UserDTO> getAllUsers(int page, int size, String sortBy, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> users = userRepository.findAll(pageable);

        List<UserDTO> content = users.getContent().stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());

        return new PaginatedResponseDTO<>(content, users.getNumber(), users.getSize(), users.getTotalElements());
    }

    @Override
    public PaginatedResponseDTO<UserDTO> getUsersByRole(String role, int page, int size) {
        UserRole userRole = UserRole.valueOf(role);
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = userRepository.findByRole(userRole, pageable);

        List<UserDTO> content = users.getContent().stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());

        return new PaginatedResponseDTO<>(content, users.getNumber(), users.getSize(), users.getTotalElements());
    }

    @Override
    public PaginatedResponseDTO<UserDTO> searchUsers(String search, int page, int size) {
        log.info("Searching users with query: {}", search);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> users = userRepository.searchUsers(search, pageable);

        List<UserDTO> content = users.getContent().stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());

        return new PaginatedResponseDTO<>(content, users.getNumber(), users.getSize(), users.getTotalElements());
    }

    @Override
    public List<UserDTO> getRecentUsers(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> users = userRepository.findAll(pageable);

        return users.getContent().stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public long countTotalUsers() {
        return userRepository.count();
    }

    @Override
    public long countUsersByRole(String role) {
        UserRole userRole = UserRole.valueOf(role);
        return userRepository.countByRole(userRole);
    }

    @Override
    public long countNewUsersToday() {
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        return userRepository.countNewUsersSince(todayStart);
    }

    @Override
    public UserDTO changePassword(String userId, ChangePasswordRequestDTO request) {
        log.info("Changing password for user {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirm password do not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user = userRepository.save(user);

        return userMapper.toDTO(user);
    }

    @Override
    public void updateProfilePicture(String userId, String profilePictureUrl) {
        log.info("Updating profile picture for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

        user.setProfilePicture(profilePictureUrl);
        userRepository.save(user);
    }

    @Override
    public UserStatsDTO getUserStats() {
        log.info("Getting user statistics");

        long totalUsers = userRepository.count();

        // Count active users (enabled = true)
        long activeUsers = userRepository.findByEnabled(true).size();

        // Count disabled users (enabled = false)
        long disabledUsers = userRepository.findByEnabled(false).size();

        // You might want to add a suspended status field to User entity
        // For now, we'll use a placeholder
        long suspendedUsers = 0; // Placeholder - can be calculated if you have a status field

        // Get roles distribution
        Map<String, Long> rolesDistribution = new HashMap<>();
        for (UserRole role : UserRole.values()) {
            long count = userRepository.countByRole(role);
            if (count > 0) {
                rolesDistribution.put(role.name(), count);
            }
        }

        // Get new users this month
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        Long newUsersThisMonth = userRepository.countNewUsersSince(monthStart);

        // Get active users this month (users who logged in this month)
        LocalDateTime monthStartForLogin = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        Long activeUsersThisMonth = userRepository.countActiveUsersSince(monthStartForLogin);

        return UserStatsDTO.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .disabledUsers(disabledUsers)
                .suspendedUsers(suspendedUsers)
                .rolesDistribution(rolesDistribution)
                .newUsersThisMonth(newUsersThisMonth != null ? newUsersThisMonth : 0L)
                .activeUsersThisMonth(activeUsersThisMonth != null ? activeUsersThisMonth : 0L)
                .build();
    }

    @Override
    public Resource exportUsers(String format) {
        log.info("Exporting users in format: {}", format);

        // Get all users
        List<User> users = userRepository.findAll();

        // Build CSV content
        StringBuilder csvContent = new StringBuilder();
        csvContent.append("ID,Email,First Name,Last Name,Phone,Role,Enabled,Created At,Last Login\n");

        for (User user : users) {
            csvContent.append(user.getId()).append(",")
                    .append(escapeCsv(user.getEmail())).append(",")
                    .append(escapeCsv(user.getFirstName())).append(",")
                    .append(escapeCsv(user.getLastName())).append(",")
                    .append(user.getPhoneNumber() != null ? escapeCsv(user.getPhoneNumber()) : "").append(",")
                    .append(user.getRole()).append(",")
                    .append(user.isEnabled()).append(",")
                    .append(user.getCreatedAt()).append(",")
                    .append(user.getLastLogin() != null ? user.getLastLogin() : "")
                    .append("\n");
        }

        // Return as Resource
        byte[] bytes = csvContent.toString().getBytes();
        return new ByteArrayResource(bytes);
    }

    // Helper method to escape CSV fields
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}