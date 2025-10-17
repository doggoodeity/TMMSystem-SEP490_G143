package tmmsystem.service;

import org.springframework.stereotype.Service;
import tmmsystem.dto.sales.CapacityCheckResultDto;
import tmmsystem.entity.*;
import tmmsystem.repository.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CapacityCheckService {
    
    private final RfqRepository rfqRepository;
    private final RfqDetailRepository rfqDetailRepository;
    private final ProductRepository productRepository;
    private final ContractRepository contractRepository;
    private final MachineRepository machineRepository;
    
    private static final BigDecimal WORKING_HOURS_PER_DAY = new BigDecimal("8"); // giờ/ngày
    
    public CapacityCheckService(RfqRepository rfqRepository,
                               RfqDetailRepository rfqDetailRepository,
                               ProductRepository productRepository,
                               ContractRepository contractRepository,
                               MachineRepository machineRepository) {
        this.rfqRepository = rfqRepository;
        this.rfqDetailRepository = rfqDetailRepository;
        this.productRepository = productRepository;
        this.contractRepository = contractRepository;
        this.machineRepository = machineRepository;
    }
    
    public CapacityCheckResultDto checkMachineCapacity(Long rfqId) {
        Rfq rfq = rfqRepository.findById(rfqId).orElseThrow();
        
        // Tính thời gian sản xuất
        LocalDate productionStartDate = rfq.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDate().plusDays(7);
        LocalDate productionEndDate = rfq.getExpectedDeliveryDate().minusDays(7);
        BigDecimal availableDays = BigDecimal.valueOf(ChronoUnit.DAYS.between(productionStartDate, productionEndDate));
        
        // Lấy chi tiết RFQ
        List<RfqDetail> rfqDetails = rfqDetailRepository.findByRfqId(rfqId);
        
        // Tính tổng khối lượng và số lượng sản phẩm
        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal totalFaceTowels = BigDecimal.ZERO;
        BigDecimal totalBathTowels = BigDecimal.ZERO;
        BigDecimal totalSportsTowels = BigDecimal.ZERO;
        
        for (RfqDetail detail : rfqDetails) {
            Product product = productRepository.findById(detail.getProduct().getId()).orElseThrow();
            BigDecimal quantity = detail.getQuantity();
            BigDecimal weight = product.getStandardWeight().divide(new BigDecimal("1000")); // gram to kg
            
            totalWeight = totalWeight.add(weight.multiply(quantity));
            
            // Phân loại sản phẩm theo tên
            String productName = product.getName().toLowerCase();
            if (productName.contains("khăn mặt")) {
                totalFaceTowels = totalFaceTowels.add(quantity);
            } else if (productName.contains("khăn tắm")) {
                totalBathTowels = totalBathTowels.add(quantity);
            } else if (productName.contains("khăn thể thao")) {
                totalSportsTowels = totalSportsTowels.add(quantity);
            }
        }
        
        // Lấy công suất máy từ database
        BigDecimal warpingCapacity = getMachineCapacity("WARPING");
        BigDecimal weavingCapacity = getMachineCapacity("WEAVING");
        BigDecimal sewingCapacity = getMachineCapacity("SEWING");
        
        // Tính công suất cần thiết
        BigDecimal warpingDays = totalWeight.divide(warpingCapacity, 2, RoundingMode.HALF_UP);
        BigDecimal weavingDays = totalWeight.divide(weavingCapacity, 2, RoundingMode.HALF_UP);
        
        // Tính công suất may (dựa trên loại sản phẩm)
        BigDecimal sewingDays = calculateSewingDays(totalFaceTowels, totalBathTowels, totalSportsTowels);
        
        // Xác định bottleneck
        BigDecimal requiredDays = warpingDays.max(weavingDays).max(sewingDays);
        String bottleneck = "WARPING";
        if (weavingDays.compareTo(warpingDays) > 0 && weavingDays.compareTo(sewingDays) > 0) {
            bottleneck = "WEAVING";
        } else if (sewingDays.compareTo(warpingDays) > 0 && sewingDays.compareTo(weavingDays) > 0) {
            bottleneck = "SEWING";
        }
        
        // Kiểm tra xung đột với đơn hàng khác
        List<CapacityCheckResultDto.ConflictDto> conflicts = checkConflicts(productionStartDate, productionEndDate, totalWeight, totalFaceTowels, totalBathTowels, totalSportsTowels);
        
        // Tạo kết quả
        CapacityCheckResultDto result = new CapacityCheckResultDto();
        
        CapacityCheckResultDto.MachineCapacityDto machineCapacity = new CapacityCheckResultDto.MachineCapacityDto();
        machineCapacity.setSufficient(requiredDays.compareTo(availableDays) <= 0 && conflicts.isEmpty());
        machineCapacity.setBottleneck(bottleneck);
        machineCapacity.setRequiredDays(requiredDays);
        machineCapacity.setAvailableDays(availableDays);
        machineCapacity.setProductionStartDate(productionStartDate);
        machineCapacity.setProductionEndDate(productionEndDate);
        machineCapacity.setConflicts(conflicts);
        
        result.setMachineCapacity(machineCapacity);
        
        // Kho luôn đủ
        CapacityCheckResultDto.WarehouseCapacityDto warehouseCapacity = new CapacityCheckResultDto.WarehouseCapacityDto();
        warehouseCapacity.setSufficient(true);
        warehouseCapacity.setMessage("Kho luôn đủ nguyên liệu");
        result.setWarehouseCapacity(warehouseCapacity);
        
        return result;
    }
    
    public CapacityCheckResultDto checkWarehouseCapacity(Long rfqId) {
        // Kho luôn đủ theo yêu cầu
        CapacityCheckResultDto result = new CapacityCheckResultDto();
        
        CapacityCheckResultDto.WarehouseCapacityDto warehouseCapacity = new CapacityCheckResultDto.WarehouseCapacityDto();
        warehouseCapacity.setSufficient(true);
        warehouseCapacity.setMessage("Kho luôn đủ nguyên liệu");
        result.setWarehouseCapacity(warehouseCapacity);
        
        return result;
    }
    
    private List<CapacityCheckResultDto.ConflictDto> checkConflicts(LocalDate startDate, LocalDate endDate, 
                                                                   BigDecimal totalWeight, BigDecimal totalFaceTowels, 
                                                                   BigDecimal totalBathTowels, BigDecimal totalSportsTowels) {
        List<CapacityCheckResultDto.ConflictDto> conflicts = new ArrayList<>();
        
        // Lấy tất cả đơn hàng đang chạy
        List<Contract> activeContracts = contractRepository.findAll().stream()
                .filter(c -> "APPROVED".equals(c.getStatus()) || "SIGNED".equals(c.getStatus()))
                .filter(c -> c.getDeliveryDate().isAfter(LocalDate.now()))
                .collect(Collectors.toList());
        
        // Kiểm tra từng ngày
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            // Lấy công suất tổng từ database
            BigDecimal totalWarpingCapacity = getMachineCapacity("WARPING");
            BigDecimal totalWeavingCapacity = getMachineCapacity("WEAVING");
            BigDecimal totalSewingCapacity = getMachineCapacity("SEWING");
            
            // Tính công suất đã sử dụng trong ngày này
            BigDecimal usedWarping = BigDecimal.ZERO;
            BigDecimal usedWeaving = BigDecimal.ZERO;
            BigDecimal usedSewing = BigDecimal.ZERO;
            
            for (Contract contract : activeContracts) {
                LocalDate contractStart = contract.getContractDate().plusDays(7);
                LocalDate contractEnd = contract.getDeliveryDate().minusDays(7);
                
                if (!currentDate.isBefore(contractStart) && !currentDate.isAfter(contractEnd)) {
                    // Đơn hàng này đang chạy trong ngày hiện tại
                    // TODO: Tính chính xác công suất đã sử dụng từ contract details
                    // Hiện tại giả định mỗi đơn hàng sử dụng 50% công suất
                    usedWarping = usedWarping.add(totalWarpingCapacity.multiply(new BigDecimal("0.5")));
                    usedWeaving = usedWeaving.add(totalWeavingCapacity.multiply(new BigDecimal("0.5")));
                    usedSewing = usedSewing.add(totalSewingCapacity.multiply(new BigDecimal("0.5")));
                }
            }
            
            // Tính công suất còn lại
            BigDecimal availableWarping = totalWarpingCapacity.subtract(usedWarping);
            BigDecimal availableWeaving = totalWeavingCapacity.subtract(usedWeaving);
            BigDecimal availableSewing = totalSewingCapacity.subtract(usedSewing);
            
            // Kiểm tra xung đột
            if (totalWeight.compareTo(availableWarping) > 0) {
                CapacityCheckResultDto.ConflictDto conflict = new CapacityCheckResultDto.ConflictDto();
                conflict.setDate(currentDate);
                conflict.setMachineType("WARPING");
                conflict.setRequired(totalWeight);
                conflict.setAvailable(availableWarping);
                conflict.setUsed(usedWarping);
                conflict.setConflictMessage("Không đủ công suất máy cuồng mắc");
                conflicts.add(conflict);
            }
            
            if (totalWeight.compareTo(availableWeaving) > 0) {
                CapacityCheckResultDto.ConflictDto conflict = new CapacityCheckResultDto.ConflictDto();
                conflict.setDate(currentDate);
                conflict.setMachineType("WEAVING");
                conflict.setRequired(totalWeight);
                conflict.setAvailable(availableWeaving);
                conflict.setUsed(usedWeaving);
                conflict.setConflictMessage("Không đủ công suất máy dệt");
                conflicts.add(conflict);
            }
            
            if (totalFaceTowels.add(totalBathTowels).add(totalSportsTowels).compareTo(availableSewing) > 0) {
                CapacityCheckResultDto.ConflictDto conflict = new CapacityCheckResultDto.ConflictDto();
                conflict.setDate(currentDate);
                conflict.setMachineType("SEWING");
                conflict.setRequired(totalFaceTowels.add(totalBathTowels).add(totalSportsTowels));
                conflict.setAvailable(availableSewing);
                conflict.setUsed(usedSewing);
                conflict.setConflictMessage("Không đủ công suất máy may");
                conflicts.add(conflict);
            }
            
            currentDate = currentDate.plusDays(1);
        }
        
        return conflicts;
    }
    
    /**
     * Lấy tổng công suất của loại máy từ database
     */
    private BigDecimal getMachineCapacity(String machineType) {
        return machineRepository.findAll().stream()
                .filter(m -> machineType.equals(m.getType()))
                .filter(m -> "AVAILABLE".equals(m.getStatus()))
                .map(this::extractCapacityFromSpecifications)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Trích xuất capacity từ JSON specifications
     */
    private BigDecimal extractCapacityFromSpecifications(Machine machine) {
        try {
            String specs = machine.getSpecifications();
            if (specs == null || specs.isEmpty()) {
                return BigDecimal.ZERO;
            }
            
            // Parse JSON để lấy capacityPerDay
            // Đơn giản: tìm "capacityPerDay":value
            String capacityPattern = "\"capacityPerDay\":";
            int startIndex = specs.indexOf(capacityPattern);
            if (startIndex == -1) {
                return BigDecimal.ZERO;
            }
            
            startIndex += capacityPattern.length();
            int endIndex = specs.indexOf(",", startIndex);
            if (endIndex == -1) {
                endIndex = specs.indexOf("}", startIndex);
            }
            
            if (endIndex == -1) {
                return BigDecimal.ZERO;
            }
            
            String capacityStr = specs.substring(startIndex, endIndex).trim();
            return new BigDecimal(capacityStr);
            
        } catch (Exception e) {
            System.err.println("Error parsing capacity from machine " + machine.getCode() + ": " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * Tính thời gian may dựa trên loại sản phẩm
     */
    private BigDecimal calculateSewingDays(BigDecimal faceTowels, BigDecimal bathTowels, BigDecimal sportsTowels) {
        // Lấy tất cả máy may
        List<Machine> sewingMachines = machineRepository.findAll().stream()
                .filter(m -> "SEWING".equals(m.getType()))
                .filter(m -> "AVAILABLE".equals(m.getStatus()))
                .collect(Collectors.toList());
        
        if (sewingMachines.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // Tính công suất theo từng loại sản phẩm
        BigDecimal totalFaceCapacity = BigDecimal.ZERO;
        BigDecimal totalBathCapacity = BigDecimal.ZERO;
        BigDecimal totalSportsCapacity = BigDecimal.ZERO;
        
        for (Machine machine : sewingMachines) {
            String specs = machine.getSpecifications();
            if (specs != null && specs.contains("capacityPerHour")) {
                // Parse capacityPerHour JSON
                BigDecimal faceCapacity = extractCapacityFromJson(specs, "faceTowels");
                BigDecimal bathCapacity = extractCapacityFromJson(specs, "bathTowels");
                BigDecimal sportsCapacity = extractCapacityFromJson(specs, "sportsTowels");
                
                totalFaceCapacity = totalFaceCapacity.add(faceCapacity.multiply(WORKING_HOURS_PER_DAY));
                totalBathCapacity = totalBathCapacity.add(bathCapacity.multiply(WORKING_HOURS_PER_DAY));
                totalSportsCapacity = totalSportsCapacity.add(sportsCapacity.multiply(WORKING_HOURS_PER_DAY));
            }
        }
        
        // Tính thời gian cần thiết cho từng loại
        BigDecimal faceDays = faceTowels.compareTo(BigDecimal.ZERO) > 0 ? 
            faceTowels.divide(totalFaceCapacity, 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal bathDays = bathTowels.compareTo(BigDecimal.ZERO) > 0 ? 
            bathTowels.divide(totalBathCapacity, 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal sportsDays = sportsTowels.compareTo(BigDecimal.ZERO) > 0 ? 
            sportsTowels.divide(totalSportsCapacity, 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        
        // Trả về thời gian lớn nhất (bottleneck)
        return faceDays.max(bathDays).max(sportsDays);
    }
    
    /**
     * Trích xuất giá trị từ JSON specifications
     */
    private BigDecimal extractCapacityFromJson(String specs, String key) {
        try {
            String pattern = "\"" + key + "\":";
            int startIndex = specs.indexOf(pattern);
            if (startIndex == -1) {
                return BigDecimal.ZERO;
            }
            
            startIndex += pattern.length();
            int endIndex = specs.indexOf(",", startIndex);
            if (endIndex == -1) {
                endIndex = specs.indexOf("}", startIndex);
            }
            
            if (endIndex == -1) {
                return BigDecimal.ZERO;
            }
            
            String valueStr = specs.substring(startIndex, endIndex).trim();
            return new BigDecimal(valueStr);
            
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}
