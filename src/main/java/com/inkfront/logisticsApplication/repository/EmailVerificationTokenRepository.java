// EmailVerificationTokenRepository.java
package com.inkfront.logisticsApplication.repository;

import com.inkfront.logisticsApplication.domain.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, String> {

    Optional<EmailVerificationToken> findByToken(String token);

    Optional<EmailVerificationToken> findByUserId(String userId);

    @Query("SELECT evt FROM EmailVerificationToken evt WHERE evt.user.id = :userId AND evt.verified = false AND evt.expiryDate > :now")
    Optional<EmailVerificationToken> findValidTokenByUser(
            @Param("userId") String userId,
            @Param("now") LocalDateTime now
    );

    @Modifying
    @Query("DELETE FROM EmailVerificationToken evt WHERE evt.expiryDate < :date")
    void deleteExpiredTokens(@Param("date") LocalDateTime date);
    @Modifying
    @Query("DELETE FROM EmailVerificationToken evt WHERE evt.user.id = :userId")
    void deleteByUserId(@Param("userId") String userId);
}