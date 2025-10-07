package tmmsystem.entity;

import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
import tmmsystem.common.BaseEntity;

@Entity @Table(name = "product_category")
@Getter @Setter
public class ProductCategory extends BaseEntity {
    @Column(nullable = false, length = 100)
    private String name;
    @Column(columnDefinition = "text")
    private String description;
}


