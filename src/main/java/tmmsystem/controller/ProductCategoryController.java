package tmmsystem.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tmmsystem.dto.ProductCategoryDto;
import tmmsystem.mapper.ProductCategoryMapper;
import tmmsystem.entity.ProductCategory;
import tmmsystem.service.ProductCategoryService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/product-categories")
public class ProductCategoryController {
    private final ProductCategoryService service;
    private final ProductCategoryMapper mapper;
    public ProductCategoryController(ProductCategoryService service, ProductCategoryMapper mapper) { this.service = service; this.mapper = mapper; }

    @GetMapping
    public List<ProductCategoryDto> list() { return service.findAll().stream().map(mapper::toDto).collect(Collectors.toList()); }

    @GetMapping("/{id}")
    public ProductCategoryDto get(@PathVariable Long id) { return mapper.toDto(service.findById(id)); }

    @PostMapping
    public ProductCategoryDto create(@RequestBody ProductCategoryDto body) { return mapper.toDto(service.create(mapper.toEntity(body))); }

    @PutMapping("/{id}")
    public ProductCategoryDto update(@PathVariable Long id, @RequestBody ProductCategoryDto body) { return mapper.toDto(service.update(id, mapper.toEntity(body))); }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}


