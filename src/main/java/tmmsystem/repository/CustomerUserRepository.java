package tmmsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tmmsystem.entity.CustomerUser;
import java.util.Optional;

public interface CustomerUserRepository extends JpaRepository<CustomerUser, Long> {
    Optional<CustomerUser> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<CustomerUser> findByEmailVerificationToken(String token);
}


