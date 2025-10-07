package tmmsystem.mapper;

import org.springframework.stereotype.Component;
import tmmsystem.dto.ProductCategoryDto;
import tmmsystem.entity.ProductCategory;

@Component
public class ProductCategoryMapper {
    public ProductCategoryDto toDto(ProductCategory e) {
        if (e == null) return null;
        ProductCategoryDto dto = new ProductCategoryDto();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setDescription(e.getDescription());
        dto.setCreatedAt(e.getCreatedAt());
        dto.setUpdatedAt(e.getUpdatedAt());
        return dto;
    }

    public ProductCategory toEntity(ProductCategoryDto dto) {
        if (dto == null) return null;
        ProductCategory e = new ProductCategory();
        e.setId(dto.getId());
        e.setName(dto.getName());
        e.setDescription(dto.getDescription());
        return e;
    }
}


