package tmmsystem.entity;

import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
import tmmsystem.common.BaseEntity;

@Entity @Table(name = "machine")
@Getter @Setter
public class Machine extends BaseEntity {
    @Column(nullable = false, unique = true, length = 50)
    private String code;
    @Column(nullable = false, length = 255)
    private String name;
    @Column(name = "type", nullable = false, length = 20)
    private String type; // SPINNING, WEAVING, DYEING, CUTTING, PACKAGING
    @Column(name = "status", length = 20)
    private String status = "AVAILABLE"; // AVAILABLE, IN_USE, MAINTENANCE, BROKEN
    @Column(name = "specifications", columnDefinition = "json")
    private String specifications; // JSON string
    @Column(name = "last_maintenance_at")
    private java.time.Instant lastMaintenanceAt;
    @Column(name = "next_maintenance_at")
    private java.time.Instant nextMaintenanceAt;
}


