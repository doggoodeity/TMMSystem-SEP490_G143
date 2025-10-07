package tmmsystem.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter @Setter
public class CustomerUserDto {
    private Long id;
    private String email;
    private String name;
    private String phoneNumber;
    private String position;
    private Long customerId;
    private Boolean isActive;
    private Boolean isVerified;
    private Boolean isPrimary;
    private Instant lastLoginAt;
    private Instant createdAt;
    private Instant updatedAt;
}


