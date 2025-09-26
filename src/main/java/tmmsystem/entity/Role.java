// entity/Role.java
package tmmsystem.entity;

import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
import tmmsystem.common.BaseEntity;

@Entity @Table(name = "roles")
@Getter @Setter
public class Role extends BaseEntity {
    @Column(nullable = false, unique = true, length = 50)
    private String name;            // ADMIN, SALES, ...
    private String description;
}
