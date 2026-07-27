package com.inkfront.logisticsApplication.service.impl;

import com.inkfront.logisticsApplication.domain.entity.User;
import com.inkfront.logisticsApplication.domain.enums.UserRole;
import com.inkfront.logisticsApplication.dto.request.user.UserUpdateRequestDTO;
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
    public UserDTO updateUser(String userId, UserUpdateRequestDTO updateRequest) {
        log.info("Updating user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

        // Check if email is being changed and is available
        if (updateRequest.getEmail() != null && !updateRequest.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(updateRequest.getEmail())) {
                throw new BadRequestException(ErrorMessages.EMAIL_ALREADY_EXISTS);
            }
        }

        // Check if phone is being changed and is available
        if (updateRequest.getPhoneNumber() != null && !updateRequest.getPhoneNumber().equals(user.getPhoneNumber())) {
            if (userRepository.existsByPhoneNumber(updateRequest.getPhoneNumber())) {
                throw new BadRequestException(ErrorMessages.PHONE_ALREADY_EXISTS);
            }
        }

        userMapper.updateUserFromDTO(updateRequest, user);
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
    public void enableUser(String userId) {
        log.info("Enabling user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

        user.setEnabled(true);
        userRepository.save(user);
    }

    @Override
    public void disableUser(String userId) {
        log.info("Disabling user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

        user.setEnabled(false);
        userRepository.save(user);
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
    public void changePassword(String userId, String oldPassword, String newPassword) {
        log.info("Changing password for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
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