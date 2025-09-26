// entity/User.java
package tmmsystem.entity;

import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
import tmmsystem.common.BaseEntity;

@Entity @Table(name = "users")
@Getter @Setter
public class User extends BaseEntity {

    private String avatar;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @Column(name = "is_verified", nullable = false)
    private Boolean verified = false;

    @Column(nullable = false)
    private String password;        // tạm thời plain; sau đổi sang BCrypt

    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    @Column(nullable = false, length = 150)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id")
    private Role role;
}
