package tmmsystem;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

@SpringBootTest
@ActiveProfiles("test")
public class CapacityCheckTest {

    @Test
    public void testCapacityCalculation() {
        // Ví dụ tính toán năng lực
        System.out.println("=== KIỂM TRA NĂNG LỰC SẢN XUẤT ===");
        
        // Dữ liệu từ RFQ
        BigDecimal totalWeight = new BigDecimal("89"); // kg
        BigDecimal faceTowels = new BigDecimal("100"); // cái
        BigDecimal bathTowels = new BigDecimal("150"); // cái
        BigDecimal sportsTowels = new BigDecimal("200"); // cái
        
        // Công suất máy
        BigDecimal warpingCapacity = new BigDecimal("400"); // kg/ngày (2 máy × 200)
        BigDecimal weavingCapacity = new BigDecimal("500"); // kg/ngày (10 máy × 50)
        BigDecimal sewingCapacity = new BigDecimal("1200"); // cái/ngày (5 máy × 150 × 8 giờ)
        
        // Tính thời gian cần thiết
        BigDecimal warpingDays = totalWeight.divide(warpingCapacity, 2, BigDecimal.ROUND_HALF_UP);
        BigDecimal weavingDays = totalWeight.divide(weavingCapacity, 2, BigDecimal.ROUND_HALF_UP);
        BigDecimal sewingDays = faceTowels.add(bathTowels).add(sportsTowels).divide(sewingCapacity, 2, BigDecimal.ROUND_HALF_UP);
        
        System.out.println("Tổng khối lượng: " + totalWeight + " kg");
        System.out.println("Tổng sản phẩm: " + faceTowels.add(bathTowels).add(sportsTowels) + " cái");
        System.out.println();
        System.out.println("Thời gian cần thiết:");
        System.out.println("- Cuồng mắc: " + warpingDays + " ngày");
        System.out.println("- Dệt: " + weavingDays + " ngày");
        System.out.println("- May: " + sewingDays + " ngày");
        
        // Xác định bottleneck
        BigDecimal bottleneck = warpingDays.max(weavingDays).max(sewingDays);
        String bottleneckType = "WARPING";
        if (weavingDays.compareTo(warpingDays) > 0 && weavingDays.compareTo(sewingDays) > 0) {
            bottleneckType = "WEAVING";
        } else if (sewingDays.compareTo(warpingDays) > 0 && sewingDays.compareTo(weavingDays) > 0) {
            bottleneckType = "SEWING";
        }
        
        System.out.println();
        System.out.println("Bottleneck: " + bottleneckType + " (" + bottleneck + " ngày)");
        
        // Kiểm tra thời gian có sẵn
        BigDecimal availableDays = new BigDecimal("3"); // Giả sử có 3 ngày
        boolean sufficient = bottleneck.compareTo(availableDays) <= 0;
        
        System.out.println("Thời gian có sẵn: " + availableDays + " ngày");
        System.out.println("Kết quả: " + (sufficient ? "ĐỦ" : "THIẾU"));
    }
}
