package com.inkfront.logisticsApplication.service.impl;

import com.inkfront.logisticsApplication.domain.entity.User;
import com.inkfront.logisticsApplication.domain.entity.PasswordResetToken;
import com.inkfront.logisticsApplication.domain.entity.EmailVerificationToken;
import com.inkfront.logisticsApplication.dto.request.auth.*;
import com.inkfront.logisticsApplication.dto.response.auth.AuthResponseDTO;
import com.inkfront.logisticsApplication.dto.response.user.UserDTO;
import com.inkfront.logisticsApplication.exception.BadRequestException;
import com.inkfront.logisticsApplication.exception.ResourceNotFoundException;
import com.inkfront.logisticsApplication.exception.UnauthorizedException;
import com.inkfront.logisticsApplication.mapper.UserMapper;
import com.inkfront.logisticsApplication.repository.UserRepository;
import com.inkfront.logisticsApplication.repository.PasswordResetTokenRepository;
import com.inkfront.logisticsApplication.repository.EmailVerificationTokenRepository;
import com.inkfront.logisticsApplication.security.JwtTokenProvider;
import com.inkfront.logisticsApplication.service.interfaces.AuthService;
import com.inkfront.logisticsApplication.service.interfaces.EmailService;
import com.inkfront.logisticsApplication.service.interfaces.GoogleAuthService;
import com.inkfront.logisticsApplication.util.CookieUtils;
import com.inkfront.logisticsApplication.domain.constants.AppConstants;
import com.inkfront.logisticsApplication.domain.constants.ErrorMessages;
import com.inkfront.logisticsApplication.domain.constants.SuccessMessages;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;
    private final EmailService emailService;
    private final GoogleAuthService googleAuthService;
    private final CookieUtils cookieUtils;

    @Override
    public AuthResponseDTO login(LoginRequestDTO loginRequest) {
        log.info("Login attempt for email: {}", loginRequest.getEmail());

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new UnauthorizedException(ErrorMessages.INVALID_CREDENTIALS));

        if (!user.isEnabled()) {
            throw new UnauthorizedException(ErrorMessages.ACCOUNT_DISABLED);
        }

        if (!user.isAccountNonLocked()) {
            if (user.getLockTime() != null && user.getLockTime().plusMinutes(AppConstants.LOCK_TIME_MINUTES).isBefore(LocalDateTime.now())) {
                // Unlock account if lock time has expired
                userRepository.resetFailedAttempts(user.getEmail());
            } else {
                throw new UnauthorizedException(ErrorMessages.ACCOUNT_LOCKED);
            }
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            userRepository.incrementFailedAttempts(user.getEmail());
            if (user.getFailedAttempts() + 1 >= AppConstants.MAX_FAILED_ATTEMPTS) {
                userRepository.lockAccount(user.getEmail(), LocalDateTime.now());
                throw new UnauthorizedException(ErrorMessages.ACCOUNT_LOCKED);
            }
            throw new UnauthorizedException(ErrorMessages.INVALID_CREDENTIALS);
        }

        // Reset failed attempts on successful login
        userRepository.resetFailedAttempts(user.getEmail());
        userRepository.updateLastLogin(user.getId(), LocalDateTime.now());

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);
        long expiresIn = jwtTokenProvider.getAccessTokenExpiration();

        AuthResponseDTO response = new AuthResponseDTO();
        response.setSuccess(true);
        response.setMessage(SuccessMessages.LOGIN_SUCCESS);
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(expiresIn);
        response.setUser(userMapper.toDTO(user));




        return response;
    }
    @Override
    public AuthResponseDTO register(RegisterRequestDTO registerRequest) {

        log.info("Registration attempt for email: {}", registerRequest.getEmail());

        User existingUser = userRepository.findByEmail(registerRequest.getEmail()).orElse(null);

        if (existingUser != null) {

            // Already verified account
            if (existingUser.isEnabled()) {
                throw new BadRequestException(ErrorMessages.EMAIL_ALREADY_EXISTS);
            }

            // Existing but not verified account
            if (registerRequest.getPhoneNumber() != null
                    && existingUser.getPhoneNumber() != null
                    && !existingUser.getPhoneNumber().equals(registerRequest.getPhoneNumber())
                    && userRepository.existsByPhoneNumber(registerRequest.getPhoneNumber())) {

                throw new BadRequestException(ErrorMessages.PHONE_ALREADY_EXISTS);
            }

            existingUser.setFirstName(registerRequest.getFirstName());
            existingUser.setLastName(registerRequest.getLastName());
            existingUser.setPhoneNumber(registerRequest.getPhoneNumber());
            existingUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            existingUser.setPreferredLanguage(registerRequest.getPreferredLanguage());
            existingUser.setThemePreference(AppConstants.DEFAULT_THEME);

            userRepository.save(existingUser);

            // Remove any previous verification links
            emailVerificationTokenRepository.deleteByUserId(existingUser.getId());

            // Create a fresh verification token
            String token = UUID.randomUUID().toString();

            EmailVerificationToken verificationToken = new EmailVerificationToken();
            verificationToken.setUser(existingUser);
            verificationToken.setToken(token);
            verificationToken.setEmail(existingUser.getEmail());
            verificationToken.setExpiryDate(
                    LocalDateTime.now().plusHours(AppConstants.VERIFICATION_TOKEN_EXPIRY_HOURS)
            );

            emailVerificationTokenRepository.save(verificationToken);

            emailService.sendVerificationEmail(existingUser.getEmail(), token);

            AuthResponseDTO response = new AuthResponseDTO();
            response.setSuccess(true);
            response.setMessage("Account already exists but is not verified. A new verification email has been sent.");
            response.setUser(userMapper.toDTO(existingUser));

            return response;
        }

        if (userRepository.existsByPhoneNumber(registerRequest.getPhoneNumber())) {
            throw new BadRequestException(ErrorMessages.PHONE_ALREADY_EXISTS);
        }

        User user = new User();
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setEmail(registerRequest.getEmail());
        user.setPhoneNumber(registerRequest.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEnabled(false);
        user.setPreferredLanguage(registerRequest.getPreferredLanguage());
        user.setThemePreference(AppConstants.DEFAULT_THEME);

        user = userRepository.save(user);

        String token = UUID.randomUUID().toString();

        EmailVerificationToken verificationToken = new EmailVerificationToken();
        verificationToken.setUser(user);
        verificationToken.setToken(token);
        verificationToken.setEmail(user.getEmail());
        verificationToken.setExpiryDate(
                LocalDateTime.now().plusHours(AppConstants.VERIFICATION_TOKEN_EXPIRY_HOURS)
        );

        emailVerificationTokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(user.getEmail(), token);

        AuthResponseDTO response = new AuthResponseDTO();
        response.setSuccess(true);
        response.setMessage(SuccessMessages.REGISTRATION_SUCCESS);
        response.setUser(userMapper.toDTO(user));

        return response;
    }
    @Override
    public AuthResponseDTO googleAuth(GoogleAuthRequestDTO googleAuthRequest) {
        log.info("Google authentication for email: {}", googleAuthRequest.getEmail());

        if (!googleAuthService.verifyGoogleToken(googleAuthRequest.getGoogleToken())) {
            throw new BadRequestException("Invalid Google token");
        }

        UserDTO userDTO = googleAuthService.createOrUpdateUserFromGoogle(googleAuthRequest);

        User user = userRepository.findById(userDTO.getId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

        userRepository.updateLastLogin(user.getId(), LocalDateTime.now());

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);
        long expiresIn = jwtTokenProvider.getAccessTokenExpiration();

        AuthResponseDTO response = new AuthResponseDTO();
        response.setSuccess(true);
        response.setMessage(SuccessMessages.LOGIN_SUCCESS);
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(expiresIn);
        response.setUser(userMapper.toDTO(user));




        return response;
    }

    @Override
    public AuthResponseDTO refreshToken(RefreshTokenRequestDTO refreshTokenRequest) {
        String refreshToken = refreshTokenRequest.getRefreshToken();

        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException(ErrorMessages.INVALID_TOKEN);
        }

        String userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

        String newAccessToken = jwtTokenProvider.generateAccessToken(user);
        long expiresIn = jwtTokenProvider.getAccessTokenExpiration();

        AuthResponseDTO response = new AuthResponseDTO();
        response.setSuccess(true);
        response.setAccessToken(newAccessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(expiresIn);
        response.setUser(userMapper.toDTO(user));

        return response;
    }

    @Override
    public void logout(String userId) {
        log.info("Logout for user ID: {}", userId);

    }

    @Override
    public void forgotPassword(ForgotPasswordRequestDTO forgotPasswordRequest) {
        log.info("Password reset requested for email: {}", forgotPasswordRequest.getEmail());

        User user = userRepository.findByEmail(forgotPasswordRequest.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

        // Generate reset token
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setToken(token);
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(AppConstants.TOKEN_EXPIRY_MINUTES));
        passwordResetTokenRepository.save(resetToken);

        emailService.sendPasswordResetEmail(user.getEmail(), token);
    }

    @Override
    public void resetPassword(ResetPasswordRequestDTO resetPasswordRequest) {
        log.info("Password reset attempt");

        if (!resetPasswordRequest.getNewPassword().equals(resetPasswordRequest.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(resetPasswordRequest.getToken())
                .orElseThrow(() -> new BadRequestException(ErrorMessages.INVALID_TOKEN));

        if (!resetToken.isValid()) {
            throw new BadRequestException(ErrorMessages.TOKEN_EXPIRED);
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(resetPasswordRequest.getNewPassword()));
        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken);
    }

    @Override
    public void verifyEmail(String token) {
        log.info("Email verification attempt");

        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException(ErrorMessages.INVALID_TOKEN));

        if (!verificationToken.isValid()) {
            throw new BadRequestException(ErrorMessages.TOKEN_EXPIRED);
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        emailVerificationTokenRepository.deleteByUserId(user.getId());
    }

    @Override
    public void resendVerificationEmail(String email) {
        log.info("Resend verification email for: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

        if (user.isEnabled()) {
            throw new BadRequestException("Email already verified");
        }

        // Delete existing tokens
        emailVerificationTokenRepository.deleteByUserId(user.getId());

        // Generate new token
        String token = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = new EmailVerificationToken();
        verificationToken.setUser(user);
        verificationToken.setToken(token);
        verificationToken.setEmail(user.getEmail());
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(AppConstants.VERIFICATION_TOKEN_EXPIRY_HOURS));
        emailVerificationTokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(user.getEmail(), token);
    }

    @Override
    public UserDTO getCurrentUser(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

        return userMapper.toDTO(user);
    }
    @Override
    public boolean validateToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }
}