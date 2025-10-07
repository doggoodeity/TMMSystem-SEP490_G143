package tmmsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tmmsystem.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}


