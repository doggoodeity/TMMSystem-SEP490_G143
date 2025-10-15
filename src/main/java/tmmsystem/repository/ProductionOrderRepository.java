package tmmsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tmmsystem.entity.ProductionOrder;

public interface ProductionOrderRepository extends JpaRepository<ProductionOrder, Long> {
    boolean existsByPoNumber(String poNumber);
}


