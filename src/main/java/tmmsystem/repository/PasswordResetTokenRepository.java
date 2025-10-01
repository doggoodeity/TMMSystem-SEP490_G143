package tmmsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tmmsystem.entity.PasswordResetToken;

import java.time.Instant;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findTopByEmailAndUsedIsFalseOrderByIdDesc(String email);
    Optional<PasswordResetToken> findByEmailAndCodeAndUsedIsFalse(String email, String code);
    void deleteByEmailOrExpiresAtBefore(String email, Instant expiresBefore);
}


