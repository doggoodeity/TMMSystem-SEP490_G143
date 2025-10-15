package tmmsystem.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.web.bind.annotation.*;
import tmmsystem.dto.sales.RfqDto;
import tmmsystem.entity.Customer;
import tmmsystem.entity.Rfq;
import tmmsystem.mapper.RfqMapper;
import tmmsystem.service.RfqService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/rfqs")
public class RfqController {
    private final RfqService service;
    private final RfqMapper mapper;

    public RfqController(RfqService service, RfqMapper mapper) { this.service = service; this.mapper = mapper; }

    @GetMapping
    public List<RfqDto> list() { return service.findAll().stream().map(mapper::toDto).collect(Collectors.toList()); }

    @GetMapping("/{id}")
    public RfqDto get(@PathVariable Long id) { return mapper.toDto(service.findById(id)); }

    @Operation(summary = "Tạo RFQ")
    @PostMapping
    public RfqDto create(
            @RequestBody(description = "Payload tạo RFQ", required = true,
                    content = @Content(schema = @Schema(implementation = RfqDto.class)))
            @org.springframework.web.bind.annotation.RequestBody RfqDto body) {
        Rfq rfq = new Rfq();
        rfq.setRfqNumber(body.getRfqNumber());
        if (body.getCustomerId() != null) { Customer c = new Customer(); c.setId(body.getCustomerId()); rfq.setCustomer(c); }
        rfq.setExpectedDeliveryDate(body.getExpectedDeliveryDate());
        rfq.setStatus(body.getStatus());
        rfq.setSent(body.getIsSent());
        rfq.setNotes(body.getNotes());
        return mapper.toDto(service.create(rfq));
    }

    @Operation(summary = "Cập nhật RFQ")
    @PutMapping("/{id}")
    public RfqDto update(
            @PathVariable Long id,
            @RequestBody(description = "Payload cập nhật RFQ", required = true,
                    content = @Content(schema = @Schema(implementation = RfqDto.class)))
            @org.springframework.web.bind.annotation.RequestBody RfqDto body) {
        Rfq rfq = new Rfq();
        rfq.setRfqNumber(body.getRfqNumber());
        if (body.getCustomerId() != null) { Customer c = new Customer(); c.setId(body.getCustomerId()); rfq.setCustomer(c); }
        rfq.setExpectedDeliveryDate(body.getExpectedDeliveryDate());
        rfq.setStatus(body.getStatus());
        rfq.setSent(body.getIsSent());
        rfq.setNotes(body.getNotes());
        return mapper.toDto(service.update(id, rfq));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { service.delete(id); }
}


