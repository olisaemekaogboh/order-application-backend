package com.inkfront.logisticsApplication.repository;

import com.inkfront.logisticsApplication.domain.entity.User;
import com.inkfront.logisticsApplication.domain.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByGoogleId(String googleId);

    Optional<User> findByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByGoogleId(String googleId);

    Page<User> findByRole(UserRole role, Pageable pageable);


    List<User> findByEnabled(boolean enabled);

    @Query("SELECT u FROM User u WHERE u.lastLogin < :date AND u.enabled = true")
    List<User> findInactiveUsers(@Param("date") LocalDateTime date);

    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    List<User> findUsersCreatedBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :date")
    Long countNewUsersSince(@Param("date") LocalDateTime date);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    Long countByRole(@Param("role") UserRole role);

    @Query("SELECT u FROM User u WHERE u.email LIKE %:searchTerm% OR u.firstName LIKE %:searchTerm% OR u.lastName LIKE %:searchTerm%")
    Page<User> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Modifying
    @Query("UPDATE User u SET u.failedAttempts = u.failedAttempts + 1 WHERE u.email = :email")
    void incrementFailedAttempts(@Param("email") String email);

    @Modifying
    @Query("UPDATE User u SET u.failedAttempts = 0, u.accountNonLocked = true, u.lockTime = null WHERE u.email = :email")
    void resetFailedAttempts(@Param("email") String email);

    @Modifying
    @Query("UPDATE User u SET u.accountNonLocked = false, u.lockTime = :lockTime WHERE u.email = :email")
    void lockAccount(@Param("email") String email, @Param("lockTime") LocalDateTime lockTime);

    @Modifying
    @Query("UPDATE User u SET u.lastLogin = :lastLogin WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") String userId, @Param("lastLogin") LocalDateTime lastLogin);
    // repository/UserRepository.java - Add these methods

    @Query("SELECT COUNT(u) FROM User u WHERE u.lastLogin >= :date")
    Long countActiveUsersSince(@Param("date") LocalDateTime date);

    // Add this method to UserRepository.java
    @Query("SELECT u FROM User u WHERE u.role = :role")
    List<User> findByRole(@Param("role") UserRole role);
}