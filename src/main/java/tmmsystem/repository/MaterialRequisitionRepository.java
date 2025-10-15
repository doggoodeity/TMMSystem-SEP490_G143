package tmmsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tmmsystem.entity.MaterialRequisition;

public interface MaterialRequisitionRepository extends JpaRepository<MaterialRequisition, Long> {
    boolean existsByRequisitionNumber(String requisitionNumber);
}


