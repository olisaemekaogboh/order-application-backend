// PasswordResetTokenRepository.java
package com.inkfront.logisticsApplication.repository;

import com.inkfront.logisticsApplication.domain.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, String> {

    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findByUserId(String userId);

    @Query("SELECT prt FROM PasswordResetToken prt WHERE prt.user.id = :userId AND prt.used = false AND prt.expiryDate > :now")
    Optional<PasswordResetToken> findValidTokenByUser(
            @Param("userId") String userId,
            @Param("now") LocalDateTime now
    );

    @Modifying
    @Query("DELETE FROM PasswordResetToken prt WHERE prt.expiryDate < :date")
    void deleteExpiredTokens(@Param("date") LocalDateTime date);
}
