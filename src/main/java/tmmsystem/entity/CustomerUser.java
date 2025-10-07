package tmmsystem.entity;

import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
import tmmsystem.common.BaseEntity;

@Entity
@Table(name = "customer_user",
       uniqueConstraints = {@UniqueConstraint(columnNames = {"customer_id", "email"})})
@Getter @Setter
public class CustomerUser extends BaseEntity {
    @Column(nullable = false, length = 150)
    private String email;
    @Column(nullable = false, length = 255)
    private String password;
    @Column(nullable = false, length = 150)
    private String name;
    @Column(name = "phone_number", length = 30)
    private String phoneNumber;
    @Column(length = 100)
    private String position;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "is_active")
    private Boolean active = true;
    @Column(name = "is_verified")
    private Boolean verified = false;
    @Column(name = "is_primary")
    private Boolean primary = false;

    @Column(name = "last_login_at")
    private java.time.Instant lastLoginAt;
    @Column(name = "password_reset_token", length = 255)
    private String passwordResetToken;
    @Column(name = "password_reset_expires_at")
    private java.time.Instant passwordResetExpiresAt;
    @Column(name = "email_verification_token", length = 255)
    private String emailVerificationToken;
}


