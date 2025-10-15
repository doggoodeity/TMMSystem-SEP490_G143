package tmmsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tmmsystem.entity.MaterialTransaction;

import java.util.List;

public interface MaterialTransactionRepository extends JpaRepository<MaterialTransaction, Long> {
    List<MaterialTransaction> findByMaterialIdOrderByCreatedAtDesc(Long materialId);
}


