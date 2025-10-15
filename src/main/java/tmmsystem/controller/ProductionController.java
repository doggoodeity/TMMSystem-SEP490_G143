package tmmsystem.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.web.bind.annotation.*;
import tmmsystem.dto.production.*;
import tmmsystem.entity.*;
import tmmsystem.mapper.ProductionMapper;
import tmmsystem.service.ProductionService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/production")
public class ProductionController {
    private final ProductionService service;
    private final ProductionMapper mapper;

    public ProductionController(ProductionService service, ProductionMapper mapper) { this.service = service; this.mapper = mapper; }

    // Production Orders
    @GetMapping("/orders")
    public List<ProductionOrderDto> listPO() { return service.findAllPO().stream().map(mapper::toDto).collect(Collectors.toList()); }
    @GetMapping("/orders/{id}")
    public ProductionOrderDto getPO(@PathVariable Long id) { return mapper.toDto(service.findPO(id)); }
    @Operation(summary = "Tạo Production Order")
    @PostMapping("/orders")
    public ProductionOrderDto createPO(
            @RequestBody(description = "Payload tạo PO", required = true,
                    content = @Content(schema = @Schema(implementation = ProductionOrderDto.class)))
            @org.springframework.web.bind.annotation.RequestBody ProductionOrderDto body) {
        ProductionOrder e = new ProductionOrder();
        if (body.getContractId() != null) { Contract c = new Contract(); c.setId(body.getContractId()); e.setContract(c); }
        e.setPoNumber(body.getPoNumber()); e.setTotalQuantity(body.getTotalQuantity());
        e.setPlannedStartDate(body.getPlannedStartDate()); e.setPlannedEndDate(body.getPlannedEndDate());
        e.setStatus(body.getStatus()); e.setPriority(body.getPriority()); e.setNotes(body.getNotes());
        return mapper.toDto(service.createPO(e));
    }
    @Operation(summary = "Cập nhật Production Order")
    @PutMapping("/orders/{id}")
    public ProductionOrderDto updatePO(@PathVariable Long id,
            @RequestBody(description = "Payload cập nhật PO", required = true,
                    content = @Content(schema = @Schema(implementation = ProductionOrderDto.class)))
            @org.springframework.web.bind.annotation.RequestBody ProductionOrderDto body) {
        ProductionOrder e = new ProductionOrder();
        if (body.getContractId() != null) { Contract c = new Contract(); c.setId(body.getContractId()); e.setContract(c); }
        e.setPoNumber(body.getPoNumber()); e.setTotalQuantity(body.getTotalQuantity());
        e.setPlannedStartDate(body.getPlannedStartDate()); e.setPlannedEndDate(body.getPlannedEndDate());
        e.setStatus(body.getStatus()); e.setPriority(body.getPriority()); e.setNotes(body.getNotes());
        return mapper.toDto(service.updatePO(id, e));
    }
    @DeleteMapping("/orders/{id}")
    public void deletePO(@PathVariable Long id) { service.deletePO(id); }

    // PO Details
    @GetMapping("/orders/{poId}/details")
    public List<ProductionOrderDetailDto> listPODetails(@PathVariable Long poId) { return service.findPODetails(poId).stream().map(mapper::toDto).collect(Collectors.toList()); }
    @GetMapping("/order-details/{id}")
    public ProductionOrderDetailDto getPODetail(@PathVariable Long id) { return mapper.toDto(service.findPODetail(id)); }
    @Operation(summary = "Thêm chi tiết PO")
    @PostMapping("/order-details")
    public ProductionOrderDetailDto createPODetail(
            @RequestBody(description = "Payload tạo chi tiết PO", required = true,
                    content = @Content(schema = @Schema(implementation = ProductionOrderDetailDto.class)))
            @org.springframework.web.bind.annotation.RequestBody ProductionOrderDetailDto body) {
        ProductionOrderDetail d = new ProductionOrderDetail();
        if (body.getProductionOrderId() != null) { ProductionOrder po = new ProductionOrder(); po.setId(body.getProductionOrderId()); d.setProductionOrder(po); }
        if (body.getProductId() != null) { Product p = new Product(); p.setId(body.getProductId()); d.setProduct(p); }
        if (body.getBomId() != null) { Bom b = new Bom(); b.setId(body.getBomId()); d.setBom(b); }
        d.setBomVersion(body.getBomVersion()); d.setQuantity(body.getQuantity()); d.setUnit(body.getUnit()); d.setNoteColor(body.getNoteColor());
        return mapper.toDto(service.createPODetail(d));
    }
    @Operation(summary = "Cập nhật chi tiết PO")
    @PutMapping("/order-details/{id}")
    public ProductionOrderDetailDto updatePODetail(@PathVariable Long id,
            @RequestBody(description = "Payload cập nhật chi tiết PO", required = true,
                    content = @Content(schema = @Schema(implementation = ProductionOrderDetailDto.class)))
            @org.springframework.web.bind.annotation.RequestBody ProductionOrderDetailDto body) {
        ProductionOrderDetail d = new ProductionOrderDetail();
        if (body.getProductionOrderId() != null) { ProductionOrder po = new ProductionOrder(); po.setId(body.getProductionOrderId()); d.setProductionOrder(po); }
        if (body.getProductId() != null) { Product p = new Product(); p.setId(body.getProductId()); d.setProduct(p); }
        if (body.getBomId() != null) { Bom b = new Bom(); b.setId(body.getBomId()); d.setBom(b); }
        d.setBomVersion(body.getBomVersion()); d.setQuantity(body.getQuantity()); d.setUnit(body.getUnit()); d.setNoteColor(body.getNoteColor());
        return mapper.toDto(service.updatePODetail(id, d));
    }
    @DeleteMapping("/order-details/{id}")
    public void deletePODetail(@PathVariable Long id) { service.deletePODetail(id); }

    // Technical Sheet
    @GetMapping("/tech-sheets/{id}")
    public TechnicalSheetDto getTechSheet(@PathVariable Long id) { return mapper.toDto(service.findTechSheet(id)); }
    @Operation(summary = "Tạo Technical Sheet")
    @PostMapping("/tech-sheets")
    public TechnicalSheetDto createTechSheet(
            @RequestBody(description = "Payload tạo Technical Sheet", required = true,
                    content = @Content(schema = @Schema(implementation = TechnicalSheetDto.class)))
            @org.springframework.web.bind.annotation.RequestBody TechnicalSheetDto body) {
        TechnicalSheet t = new TechnicalSheet();
        if (body.getProductionOrderId() != null) { ProductionOrder po = new ProductionOrder(); po.setId(body.getProductionOrderId()); t.setProductionOrder(po); }
        t.setSheetNumber(body.getSheetNumber()); t.setYarnSpecifications(body.getYarnSpecifications()); t.setMachineSettings(body.getMachineSettings());
        t.setQualityStandards(body.getQualityStandards()); t.setSpecialInstructions(body.getSpecialInstructions());
        return mapper.toDto(service.createTechSheet(t));
    }
    @Operation(summary = "Cập nhật Technical Sheet")
    @PutMapping("/tech-sheets/{id}")
    public TechnicalSheetDto updateTechSheet(@PathVariable Long id,
            @RequestBody(description = "Payload cập nhật Technical Sheet", required = true,
                    content = @Content(schema = @Schema(implementation = TechnicalSheetDto.class)))
            @org.springframework.web.bind.annotation.RequestBody TechnicalSheetDto body) {
        TechnicalSheet t = new TechnicalSheet();
        if (body.getProductionOrderId() != null) { ProductionOrder po = new ProductionOrder(); po.setId(body.getProductionOrderId()); t.setProductionOrder(po); }
        t.setSheetNumber(body.getSheetNumber()); t.setYarnSpecifications(body.getYarnSpecifications()); t.setMachineSettings(body.getMachineSettings());
        t.setQualityStandards(body.getQualityStandards()); t.setSpecialInstructions(body.getSpecialInstructions());
        return mapper.toDto(service.updateTechSheet(id, t));
    }
    @DeleteMapping("/tech-sheets/{id}")
    public void deleteTechSheet(@PathVariable Long id) { service.deleteTechSheet(id); }

    // Work Orders
    @GetMapping("/orders/{poId}/work-orders")
    public List<WorkOrderDto> listWOs(@PathVariable Long poId) { return service.findWOs(poId).stream().map(mapper::toDto).collect(Collectors.toList()); }
    @GetMapping("/work-orders/{id}")
    public WorkOrderDto getWO(@PathVariable Long id) { return mapper.toDto(service.findWO(id)); }
    @Operation(summary = "Tạo Work Order")
    @PostMapping("/work-orders")
    public WorkOrderDto createWO(
            @RequestBody(description = "Payload tạo WO", required = true,
                    content = @Content(schema = @Schema(implementation = WorkOrderDto.class)))
            @org.springframework.web.bind.annotation.RequestBody WorkOrderDto body) {
        WorkOrder w = new WorkOrder();
        if (body.getProductionOrderId() != null) { ProductionOrder po = new ProductionOrder(); po.setId(body.getProductionOrderId()); w.setProductionOrder(po); }
        w.setWoNumber(body.getWoNumber()); w.setDeadline(body.getDeadline()); w.setStatus(body.getStatus()); w.setSendStatus(body.getSendStatus()); w.setProduction(body.getIsProduction());
        return mapper.toDto(service.createWO(w));
    }
    @Operation(summary = "Cập nhật Work Order")
    @PutMapping("/work-orders/{id}")
    public WorkOrderDto updateWO(@PathVariable Long id,
            @RequestBody(description = "Payload cập nhật WO", required = true,
                    content = @Content(schema = @Schema(implementation = WorkOrderDto.class)))
            @org.springframework.web.bind.annotation.RequestBody WorkOrderDto body) {
        WorkOrder w = new WorkOrder();
        if (body.getProductionOrderId() != null) { ProductionOrder po = new ProductionOrder(); po.setId(body.getProductionOrderId()); w.setProductionOrder(po); }
        w.setWoNumber(body.getWoNumber()); w.setDeadline(body.getDeadline()); w.setStatus(body.getStatus()); w.setSendStatus(body.getSendStatus()); w.setProduction(body.getIsProduction());
        return mapper.toDto(service.updateWO(id, w));
    }
    @DeleteMapping("/work-orders/{id}")
    public void deleteWO(@PathVariable Long id) { service.deleteWO(id); }

    // Work Order Details
    @GetMapping("/work-orders/{woId}/details")
    public List<WorkOrderDetailDto> listWODetails(@PathVariable Long woId) { return service.findWODetails(woId).stream().map(mapper::toDto).collect(Collectors.toList()); }
    @GetMapping("/work-order-details/{id}")
    public WorkOrderDetailDto getWODetail(@PathVariable Long id) { return mapper.toDto(service.findWODetail(id)); }
    @Operation(summary = "Thêm chi tiết WO")
    @PostMapping("/work-order-details")
    public WorkOrderDetailDto createWODetail(
            @RequestBody(description = "Payload tạo chi tiết WO", required = true,
                    content = @Content(schema = @Schema(implementation = WorkOrderDetailDto.class)))
            @org.springframework.web.bind.annotation.RequestBody WorkOrderDetailDto body) {
        WorkOrderDetail d = new WorkOrderDetail();
        if (body.getWorkOrderId() != null) { WorkOrder w = new WorkOrder(); w.setId(body.getWorkOrderId()); d.setWorkOrder(w); }
        if (body.getProductionOrderDetailId() != null) { ProductionOrderDetail pod = new ProductionOrderDetail(); pod.setId(body.getProductionOrderDetailId()); d.setProductionOrderDetail(pod); }
        d.setStageSequence(body.getStageSequence()); d.setPlannedStartAt(body.getPlannedStartAt()); d.setPlannedEndAt(body.getPlannedEndAt());
        d.setStartAt(body.getStartAt()); d.setCompleteAt(body.getCompleteAt()); d.setWorkStatus(body.getWorkStatus()); d.setNotes(body.getNotes());
        return mapper.toDto(service.createWODetail(d));
    }
    @Operation(summary = "Cập nhật chi tiết WO")
    @PutMapping("/work-order-details/{id}")
    public WorkOrderDetailDto updateWODetail(@PathVariable Long id,
            @RequestBody(description = "Payload cập nhật chi tiết WO", required = true,
                    content = @Content(schema = @Schema(implementation = WorkOrderDetailDto.class)))
            @org.springframework.web.bind.annotation.RequestBody WorkOrderDetailDto body) {
        WorkOrderDetail d = new WorkOrderDetail();
        if (body.getWorkOrderId() != null) { WorkOrder w = new WorkOrder(); w.setId(body.getWorkOrderId()); d.setWorkOrder(w); }
        if (body.getProductionOrderDetailId() != null) { ProductionOrderDetail pod = new ProductionOrderDetail(); pod.setId(body.getProductionOrderDetailId()); d.setProductionOrderDetail(pod); }
        d.setStageSequence(body.getStageSequence()); d.setPlannedStartAt(body.getPlannedStartAt()); d.setPlannedEndAt(body.getPlannedEndAt());
        d.setStartAt(body.getStartAt()); d.setCompleteAt(body.getCompleteAt()); d.setWorkStatus(body.getWorkStatus()); d.setNotes(body.getNotes());
        return mapper.toDto(service.updateWODetail(id, d));
    }
    @DeleteMapping("/work-order-details/{id}")
    public void deleteWODetail(@PathVariable Long id) { service.deleteWODetail(id); }

    // Stages
    @GetMapping("/work-order-details/{woDetailId}/stages")
    public List<ProductionStageDto> listStages(@PathVariable Long woDetailId) { return service.findStages(woDetailId).stream().map(mapper::toDto).collect(Collectors.toList()); }
    @GetMapping("/stages/{id}")
    public ProductionStageDto getStage(@PathVariable Long id) { return mapper.toDto(service.findStage(id)); }
    @Operation(summary = "Tạo Production Stage")
    @PostMapping("/stages")
    public ProductionStageDto createStage(
            @RequestBody(description = "Payload tạo Stage", required = true,
                    content = @Content(schema = @Schema(implementation = ProductionStageDto.class)))
            @org.springframework.web.bind.annotation.RequestBody ProductionStageDto body) {
        ProductionStage s = new ProductionStage();
        if (body.getWorkOrderDetailId() != null) { WorkOrderDetail d = new WorkOrderDetail(); d.setId(body.getWorkOrderDetailId()); s.setWorkOrderDetail(d); }
        s.setStageType(body.getStageType()); s.setStageSequence(body.getStageSequence());
        if (body.getMachineId() != null) { Machine m = new Machine(); m.setId(body.getMachineId()); s.setMachine(m); }
        if (body.getAssignedToId() != null) { User u = new User(); u.setId(body.getAssignedToId()); s.setAssignedTo(u); }
        if (body.getAssignedLeaderId() != null) { User ul = new User(); ul.setId(body.getAssignedLeaderId()); s.setAssignedLeader(ul); }
        s.setBatchNumber(body.getBatchNumber()); s.setPlannedOutput(body.getPlannedOutput()); s.setActualOutput(body.getActualOutput());
        s.setStartAt(body.getStartAt()); s.setCompleteAt(body.getCompleteAt()); s.setStatus(body.getStatus());
        s.setOutsourced(body.getIsOutsourced()); s.setOutsourceVendor(body.getOutsourceVendor()); s.setNotes(body.getNotes());
        return mapper.toDto(service.createStage(s));
    }
    @Operation(summary = "Cập nhật Production Stage")
    @PutMapping("/stages/{id}")
    public ProductionStageDto updateStage(@PathVariable Long id,
            @RequestBody(description = "Payload cập nhật Stage", required = true,
                    content = @Content(schema = @Schema(implementation = ProductionStageDto.class)))
            @org.springframework.web.bind.annotation.RequestBody ProductionStageDto body) {
        ProductionStage s = new ProductionStage();
        if (body.getWorkOrderDetailId() != null) { WorkOrderDetail d = new WorkOrderDetail(); d.setId(body.getWorkOrderDetailId()); s.setWorkOrderDetail(d); }
        s.setStageType(body.getStageType()); s.setStageSequence(body.getStageSequence());
        if (body.getMachineId() != null) { Machine m = new Machine(); m.setId(body.getMachineId()); s.setMachine(m); }
        if (body.getAssignedToId() != null) { User u = new User(); u.setId(body.getAssignedToId()); s.setAssignedTo(u); }
        if (body.getAssignedLeaderId() != null) { User ul = new User(); ul.setId(body.getAssignedLeaderId()); s.setAssignedLeader(ul); }
        s.setBatchNumber(body.getBatchNumber()); s.setPlannedOutput(body.getPlannedOutput()); s.setActualOutput(body.getActualOutput());
        s.setStartAt(body.getStartAt()); s.setCompleteAt(body.getCompleteAt()); s.setStatus(body.getStatus());
        s.setOutsourced(body.getIsOutsourced()); s.setOutsourceVendor(body.getOutsourceVendor()); s.setNotes(body.getNotes());
        return mapper.toDto(service.updateStage(id, s));
    }
    @DeleteMapping("/stages/{id}")
    public void deleteStage(@PathVariable Long id) { service.deleteStage(id); }
}


