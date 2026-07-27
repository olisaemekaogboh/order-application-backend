package com.inkfront.logisticsApplication.service.impl;

import com.inkfront.logisticsApplication.domain.entity.User;
import com.inkfront.logisticsApplication.domain.enums.UserRole;
import com.inkfront.logisticsApplication.dto.request.user.ChangePasswordRequestDTO;
import com.inkfront.logisticsApplication.dto.request.user.UpdateProfileRequestDTO;
import com.inkfront.logisticsApplication.dto.request.user.UserRoleUpdateRequestDTO;
import com.inkfront.logisticsApplication.dto.request.user.UserStatusUpdateRequestDTO;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import com.inkfront.logisticsApplication.dto.response.user.UserDTO;
import com.inkfront.logisticsApplication.exception.BadRequestException;
import com.inkfront.logisticsApplication.exception.ResourceNotFoundException;
import com.inkfront.logisticsApplication.mapper.UserMapper;
import com.inkfront.logisticsApplication.repository.UserRepository;
import com.inkfront.logisticsApplication.service.interfaces.UserService;
import com.inkfront.logisticsApplication.domain.constants.ErrorMessages;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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
}