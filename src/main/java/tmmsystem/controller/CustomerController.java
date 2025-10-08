package tmmsystem.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import tmmsystem.dto.CustomerDto;
import tmmsystem.dto.CustomerCreateRequest;
import tmmsystem.entity.Customer;
import tmmsystem.mapper.CustomerMapper;
import tmmsystem.service.CustomerService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/customers")
@Validated
public class CustomerController {
    private final CustomerService service;
    private final CustomerMapper mapper;
    public CustomerController(CustomerService service, CustomerMapper mapper) { this.service = service; this.mapper = mapper; }

    @GetMapping
    public List<CustomerDto> list() { return service.findAll().stream().map(mapper::toDto).collect(Collectors.toList()); }

    @GetMapping("/{id}")
    public CustomerDto get(@PathVariable Long id) { return mapper.toDto(service.findById(id)); }

    @PostMapping
    public CustomerDto create(@Valid @RequestBody CustomerCreateRequest request) {
        // Tạo Customer entity từ request (không có ID và timestamp)
        Customer customer = new Customer();
        customer.setCompanyName(request.companyName());
        customer.setContactPerson(request.contactPerson());
        customer.setEmail(request.email());
        customer.setPhoneNumber(request.phoneNumber());
        customer.setAddress(request.address());
        customer.setTaxCode(request.taxCode());
        customer.setActive(request.isActive() != null ? request.isActive() : true);
        customer.setVerified(request.isVerified() != null ? request.isVerified() : false);
        customer.setRegistrationType(request.registrationType() != null ? request.registrationType() : "SALES_CREATED");
        
        Customer created = service.create(customer, request.createdById());
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


