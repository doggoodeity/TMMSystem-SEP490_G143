package tmmsystem.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter @Setter
public class CustomerDto {
    private Long id;
    private String companyName;
    private String contactPerson;
    private String email;
    private String phoneNumber;
    private String address;
    private String taxCode;
    private Boolean isActive;
    private Boolean isVerified;
    private String registrationType;
    private Long createdById;
    private Instant createdAt;
    private Instant updatedAt;
}


