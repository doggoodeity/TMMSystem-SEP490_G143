package tmmsystem.entity;

import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
import tmmsystem.common.BaseEntity;

@Entity @Table(name = "customer")
@Getter @Setter
public class Customer extends BaseEntity {
    @Column(name = "company_name", nullable = false, length = 255)
    private String companyName;
    @Column(name = "contact_person", length = 150)
    private String contactPerson;
    @Column(length = 150)
    private String email;
    @Column(name = "phone_number", length = 30)
    private String phoneNumber;
    @Column(columnDefinition = "text")
    private String address;
    @Column(name = "tax_code", length = 50)
    private String taxCode;
    @Column(name = "is_active")
    private Boolean active = true;
    @Column(name = "is_verified")
    private Boolean verified = false;
    @Column(name = "registration_type", length = 20)
    private String registrationType = "SALES_CREATED";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
}


