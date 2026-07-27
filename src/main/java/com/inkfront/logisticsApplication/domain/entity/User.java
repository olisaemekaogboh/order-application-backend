package com.inkfront.logisticsApplication.domain.entity;

import com.inkfront.logisticsApplication.domain.enums.UserRole;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "phone_number", unique = true)
    private String phoneNumber;

    @Column(name = "profile_picture")
    private String profilePicture;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role = UserRole.CLIENT;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "google_auth")
    private boolean googleAuth = false;

    @Column(name = "google_id")
    private String googleId;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "preferred_language")
    private String preferredLanguage = "en";

    @Column(name = "theme_preference")
    private String themePreference = "light";

    @Column(name = "account_non_locked")
    private boolean accountNonLocked = true;

    @Column(name = "failed_attempts")
    private int failedAttempts = 0;

    @Column(name = "lock_time")
    private LocalDateTime lockTime;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orders = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DeliveryAddress> addresses = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Notification> notifications = new ArrayList<>();

    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<EmailVerificationToken> emailVerificationTokens = new ArrayList<>();

    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<PasswordResetToken> passwordResetTokens = new ArrayList<>();
    public String getFullName() {
        return firstName + " " + lastName;
    }
}