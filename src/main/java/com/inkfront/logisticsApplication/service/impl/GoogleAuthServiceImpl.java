// service/impl/GoogleAuthServiceImpl.java
package com.inkfront.logisticsApplication.service.impl;

import com.inkfront.logisticsApplication.domain.entity.User;
import com.inkfront.logisticsApplication.domain.enums.UserRole;
import com.inkfront.logisticsApplication.dto.request.auth.GoogleAuthRequestDTO;
import com.inkfront.logisticsApplication.dto.response.user.UserDTO;
import com.inkfront.logisticsApplication.exception.BadRequestException;
import com.inkfront.logisticsApplication.exception.DuplicateResourceException;
import com.inkfront.logisticsApplication.mapper.UserMapper;
import com.inkfront.logisticsApplication.repository.UserRepository;
import com.inkfront.logisticsApplication.service.interfaces.GoogleAuthService;
import com.inkfront.logisticsApplication.service.interfaces.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class GoogleAuthServiceImpl implements GoogleAuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final EmailService emailService;
    private final RestTemplate restTemplate;


    @Override
    public UserDTO authenticateWithGoogle(GoogleAuthRequestDTO request) {

        log.info("Authenticating Google user: {}", request.getEmail());

        verifyGoogleToken(request);

        return createOrUpdateUserFromGoogle(request);
    }

    @Override
    public boolean verifyGoogleToken(String accessToken) {
        try {

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response =
                    restTemplate.exchange(
                            "https://www.googleapis.com/oauth2/v3/userinfo",
                            HttpMethod.GET,
                            entity,
                            Map.class);

            return response.getStatusCode().is2xxSuccessful()
                    && response.getBody() != null
                    && response.getBody().get("email") != null;

        } catch (Exception ex) {
            log.error("Google token verification failed", ex);
            return false;
        }
    }
    private void verifyGoogleToken(GoogleAuthRequestDTO request) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(request.getGoogleToken());

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response =
                restTemplate.exchange(
                        "https://www.googleapis.com/oauth2/v3/userinfo",
                        HttpMethod.GET,
                        entity,
                        Map.class);

        if (!response.getStatusCode().is2xxSuccessful()
                || response.getBody() == null) {

            throw new BadRequestException("Invalid Google token");
        }

        Map<String, Object> profile = response.getBody();

        String email = (String) profile.get("email");
        String sub = (String) profile.get("sub");

        if (!request.getEmail().equals(email)) {
            throw new BadRequestException("Google email mismatch");
        }

        if (!request.getGoogleId().equals(sub)) {
            throw new BadRequestException("Google ID mismatch");
        }
    }
    @Override
    public UserDTO createOrUpdateUserFromGoogle(GoogleAuthRequestDTO googleAuthRequest) {
        log.info("Creating or updating user from Google for email: {}", googleAuthRequest.getEmail());

        Optional<User> existingUser = userRepository.findByEmail(googleAuthRequest.getEmail());
        User user;

        if (existingUser.isPresent()) {
            // Update existing user
            user = existingUser.get();

            // Check if email is already registered with password (not Google)
            if (!user.isGoogleAuth() && user.getPassword() != null) {
                throw new DuplicateResourceException(
                        "Email already registered with password. Please login with password."
                );
            }

            // Update Google info
            user.setGoogleId(googleAuthRequest.getGoogleId());
            user.setGoogleAuth(true);
            user.setProfilePicture(googleAuthRequest.getPicture() != null ?
                    googleAuthRequest.getPicture() : user.getProfilePicture());
            user.setLastName(extractLastName(googleAuthRequest.getName()));
            user.setFirstName(extractFirstName(googleAuthRequest.getName()));
            user.setLastLogin(LocalDateTime.now());
            user.setEnabled(true); // Google users are automatically verified

            log.info("Updated existing user from Google: {}", user.getEmail());
        } else {
            // Create new user
            user = new User();
            user.setEmail(googleAuthRequest.getEmail());
            user.setGoogleId(googleAuthRequest.getGoogleId());
            user.setGoogleAuth(true);
            user.setFirstName(extractFirstName(googleAuthRequest.getName()));
            user.setLastName(extractLastName(googleAuthRequest.getName()));
            user.setProfilePicture(googleAuthRequest.getPicture());
            user.setEnabled(true);
            user.setRole(UserRole.CLIENT);
            user.setLastLogin(LocalDateTime.now());
            user.setPreferredLanguage("en");
            user.setThemePreference("light");

            log.info("Created new user from Google: {}", user.getEmail());
        }

        user = userRepository.save(user);

        // Send welcome email if new user
        if (!existingUser.isPresent()) {
            emailService.sendWelcomeEmail(user.getEmail(), user.getFirstName());
        }

        return userMapper.toDTO(user);
    }

    @Override
    public void disconnectGoogleAccount(String userId) {
        log.info("Disconnecting Google account for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setGoogleId(null);
        user.setGoogleAuth(false);

        // If user has no password, they need to set one
        if (user.getPassword() == null) {
            throw new BadRequestException(
                    "User must set a password before disconnecting Google account"
            );
        }

        userRepository.save(user);
        log.info("Google account disconnected for user: {}", userId);
    }

    private String extractFirstName(String fullName) {
        if (fullName == null || fullName.isEmpty()) {
            return "Google";
        }
        String[] parts = fullName.split(" ");
        return parts[0];
    }

    private String extractLastName(String fullName) {
        if (fullName == null || fullName.isEmpty()) {
            return "User";
        }
        String[] parts = fullName.split(" ");
        if (parts.length > 1) {
            return String.join(" ", java.util.Arrays.copyOfRange(parts, 1, parts.length));
        }
        return "";
    }
}