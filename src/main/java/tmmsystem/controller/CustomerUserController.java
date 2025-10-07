package tmmsystem.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tmmsystem.dto.CustomerUserDto;
import tmmsystem.entity.CustomerUser;
import tmmsystem.mapper.CustomerUserMapper;
import tmmsystem.service.CustomerUserService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/customer-users")
public class CustomerUserController {
    private final CustomerUserService service;
    private final CustomerUserMapper mapper;
    public CustomerUserController(CustomerUserService service, CustomerUserMapper mapper) { this.service = service; this.mapper = mapper; }

    @GetMapping
    public List<CustomerUserDto> list() { return service.findAll().stream().map(mapper::toDto).collect(Collectors.toList()); }

    @GetMapping("/{id}")
    public CustomerUserDto get(@PathVariable Long id) { return mapper.toDto(service.findById(id)); }

    @PostMapping
    public CustomerUserDto create(@RequestBody CustomerUserDto body) {
        CustomerUser created = service.create(mapper.toEntity(body), body.getCustomerId());
        return mapper.toDto(created);
    }

    @PutMapping("/{id}")
    public CustomerUserDto update(@PathVariable Long id, @RequestBody CustomerUserDto body) {
        CustomerUser updated = service.update(id, mapper.toEntity(body), body.getCustomerId());
        return mapper.toDto(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}


