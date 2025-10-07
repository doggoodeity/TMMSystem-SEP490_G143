package tmmsystem.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tmmsystem.dto.CustomerDto;
import tmmsystem.entity.Customer;
import tmmsystem.mapper.CustomerMapper;
import tmmsystem.service.CustomerService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/customers")
public class CustomerController {
    private final CustomerService service;
    private final CustomerMapper mapper;
    public CustomerController(CustomerService service, CustomerMapper mapper) { this.service = service; this.mapper = mapper; }

    @GetMapping
    public List<CustomerDto> list() { return service.findAll().stream().map(mapper::toDto).collect(Collectors.toList()); }

    @GetMapping("/{id}")
    public CustomerDto get(@PathVariable Long id) { return mapper.toDto(service.findById(id)); }

    @PostMapping
    public CustomerDto create(@RequestBody CustomerDto body) {
        Customer created = service.create(mapper.toEntity(body), body.getCreatedById());
        return mapper.toDto(created);
    }

    @PutMapping("/{id}")
    public CustomerDto update(@PathVariable Long id, @RequestBody CustomerDto body) {
        Customer updated = service.update(id, mapper.toEntity(body));
        return mapper.toDto(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}


