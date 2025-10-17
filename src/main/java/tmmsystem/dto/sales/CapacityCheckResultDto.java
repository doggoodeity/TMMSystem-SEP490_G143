package tmmsystem.dto.sales;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class CapacityCheckResultDto {
    private MachineCapacityDto machineCapacity;
    private WarehouseCapacityDto warehouseCapacity;
    
    @Getter
    @Setter
    public static class MachineCapacityDto {
        private boolean sufficient;
        private String bottleneck;
        private BigDecimal requiredDays;
        private BigDecimal availableDays;
        private LocalDate productionStartDate;
        private LocalDate productionEndDate;
        private List<DailyCapacityDto> dailyCapacities;
        private List<ConflictDto> conflicts;
    }
    
    @Getter
    @Setter
    public static class WarehouseCapacityDto {
        private boolean sufficient;
        private String message;
    }
    
    @Getter
    @Setter
    public static class DailyCapacityDto {
        private LocalDate date;
        private BigDecimal warpingRequired;
        private BigDecimal warpingAvailable;
        private BigDecimal weavingRequired;
        private BigDecimal weavingAvailable;
        private BigDecimal sewingRequired;
        private BigDecimal sewingAvailable;
    }
    
    @Getter
    @Setter
    public static class ConflictDto {
        private LocalDate date;
        private String machineType;
        private BigDecimal required;
        private BigDecimal available;
        private BigDecimal used;
        private String conflictMessage;
    }
}
