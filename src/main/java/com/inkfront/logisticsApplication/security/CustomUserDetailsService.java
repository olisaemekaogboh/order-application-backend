package com.inkfront.logisticsApplication.security;

import com.inkfront.logisticsApplication.domain.entity.User;
import com.inkfront.logisticsApplication.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with email: " + email));

        // Debug logging
        log.info("Loading user: {}", email);
        log.info("Google Auth: {}", user.isGoogleAuth());
        log.info("Password is null: {}", user.getPassword() == null);
        log.info("Role: {}", user.getRole());

        /*
         * Google-authenticated users may not have a local password.
         * Spring Security's UserDetails requires a non-null password,
         * so provide a harmless placeholder.
         */
        String password = user.getPassword();

        if (password == null || password.isBlank()) {
            password = "{noop}GOOGLE_AUTH";
        }

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                password,
                user.isEnabled(),
                true,
                true,
                user.isAccountNonLocked(),
                Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
                )
        );
    }

    public UserDetails loadUserByUserId(String userId) throws UsernameNotFoundException {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with ID: " + userId));

        return loadUserByUsername(user.getEmail());
    }

    public boolean isUserEnabled(String email) {

        return userRepository.findByEmail(email)
                .map(User::isEnabled)
                .orElse(false);
    }

    public boolean isUserAccountNonLocked(String email) {

        return userRepository.findByEmail(email)
                .map(User::isAccountNonLocked)
                .orElse(false);
    }
}