package tmmsystem.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.web.bind.annotation.*;
import tmmsystem.dto.sales.QuotationDto;
import tmmsystem.entity.Customer;
import tmmsystem.entity.Quotation;
import tmmsystem.entity.Rfq;
import tmmsystem.mapper.QuotationMapper;
import tmmsystem.service.QuotationService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/quotations")
public class QuotationController {
    private final QuotationService service;
    private final QuotationMapper mapper;

    public QuotationController(QuotationService service, QuotationMapper mapper) { this.service = service; this.mapper = mapper; }

    @GetMapping
    public List<QuotationDto> list() { return service.findAll().stream().map(mapper::toDto).collect(Collectors.toList()); }

    @GetMapping("/{id}")
    public QuotationDto get(@PathVariable Long id) { return mapper.toDto(service.findById(id)); }

    @Operation(summary = "Tạo báo giá")
    @PostMapping
    public QuotationDto create(
            @RequestBody(description = "Payload tạo báo giá", required = true,
                    content = @Content(schema = @Schema(implementation = QuotationDto.class)))
            @org.springframework.web.bind.annotation.RequestBody QuotationDto body) {
        Quotation q = new Quotation();
        q.setQuotationNumber(body.getQuotationNumber());
        if (body.getRfqId() != null) { Rfq r = new Rfq(); r.setId(body.getRfqId()); q.setRfq(r); }
        if (body.getCustomerId() != null) { Customer c = new Customer(); c.setId(body.getCustomerId()); q.setCustomer(c); }
        q.setValidUntil(body.getValidUntil());
        q.setTotalAmount(body.getTotalAmount());
        q.setStatus(body.getStatus());
        q.setAccepted(body.getIsAccepted());
        q.setCanceled(body.getIsCanceled());
        q.setCapacityCheckedAt(body.getCapacityCheckedAt());
        q.setCapacityCheckNotes(body.getCapacityCheckNotes());
        return mapper.toDto(service.create(q));
    }

    @Operation(summary = "Cập nhật báo giá")
    @PutMapping("/{id}")
    public QuotationDto update(
            @PathVariable Long id,
            @RequestBody(description = "Payload cập nhật báo giá", required = true,
                    content = @Content(schema = @Schema(implementation = QuotationDto.class)))
            @org.springframework.web.bind.annotation.RequestBody QuotationDto body) {
        Quotation q = new Quotation();
        q.setQuotationNumber(body.getQuotationNumber());
        if (body.getRfqId() != null) { Rfq r = new Rfq(); r.setId(body.getRfqId()); q.setRfq(r); }
        if (body.getCustomerId() != null) { Customer c = new Customer(); c.setId(body.getCustomerId()); q.setCustomer(c); }
        q.setValidUntil(body.getValidUntil());
        q.setTotalAmount(body.getTotalAmount());
        q.setStatus(body.getStatus());
        q.setAccepted(body.getIsAccepted());
        q.setCanceled(body.getIsCanceled());
        q.setCapacityCheckedAt(body.getCapacityCheckedAt());
        q.setCapacityCheckNotes(body.getCapacityCheckNotes());
        return mapper.toDto(service.update(id, q));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { service.delete(id); }
}


