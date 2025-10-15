package tmmsystem.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.web.bind.annotation.*;
import tmmsystem.dto.sales.ContractDto;
import tmmsystem.entity.Customer;
import tmmsystem.entity.Quotation;
import tmmsystem.entity.Contract;
import tmmsystem.mapper.ContractMapper;
import tmmsystem.service.ContractService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/contracts")
public class ContractController {
    private final ContractService service;
    private final ContractMapper mapper;

    public ContractController(ContractService service, ContractMapper mapper) { this.service = service; this.mapper = mapper; }

    @GetMapping
    public List<ContractDto> list() { return service.findAll().stream().map(mapper::toDto).collect(Collectors.toList()); }

    @GetMapping("/{id}")
    public ContractDto get(@PathVariable Long id) { return mapper.toDto(service.findById(id)); }

    @Operation(summary = "Tạo hợp đồng")
    @PostMapping
    public ContractDto create(
            @RequestBody(description = "Payload tạo hợp đồng", required = true,
                    content = @Content(schema = @Schema(implementation = ContractDto.class)))
            @org.springframework.web.bind.annotation.RequestBody ContractDto body) {
        Contract c = new Contract();
        c.setContractNumber(body.getContractNumber());
        if (body.getQuotationId() != null) { Quotation q = new Quotation(); q.setId(body.getQuotationId()); c.setQuotation(q); }
        if (body.getCustomerId() != null) { Customer cust = new Customer(); cust.setId(body.getCustomerId()); c.setCustomer(cust); }
        c.setContractDate(body.getContractDate());
        c.setDeliveryDate(body.getDeliveryDate());
        c.setTotalAmount(body.getTotalAmount());
        c.setFilePath(body.getFilePath());
        c.setStatus(body.getStatus());
        c.setDirectorApprovedAt(body.getDirectorApprovedAt());
        c.setDirectorApprovalNotes(body.getDirectorApprovalNotes());
        return mapper.toDto(service.create(c));
    }

    @Operation(summary = "Cập nhật hợp đồng")
    @PutMapping("/{id}")
    public ContractDto update(
            @PathVariable Long id,
            @RequestBody(description = "Payload cập nhật hợp đồng", required = true,
                    content = @Content(schema = @Schema(implementation = ContractDto.class)))
            @org.springframework.web.bind.annotation.RequestBody ContractDto body) {
        Contract c = new Contract();
        c.setContractNumber(body.getContractNumber());
        if (body.getQuotationId() != null) { Quotation q = new Quotation(); q.setId(body.getQuotationId()); c.setQuotation(q); }
        if (body.getCustomerId() != null) { Customer cust = new Customer(); cust.setId(body.getCustomerId()); c.setCustomer(cust); }
        c.setContractDate(body.getContractDate());
        c.setDeliveryDate(body.getDeliveryDate());
        c.setTotalAmount(body.getTotalAmount());
        c.setFilePath(body.getFilePath());
        c.setStatus(body.getStatus());
        c.setDirectorApprovedAt(body.getDirectorApprovedAt());
        c.setDirectorApprovalNotes(body.getDirectorApprovalNotes());
        return mapper.toDto(service.update(id, c));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { service.delete(id); }
}


