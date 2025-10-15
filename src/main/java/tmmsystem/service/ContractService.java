package tmmsystem.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tmmsystem.entity.Contract;
import tmmsystem.repository.ContractRepository;

import java.util.List;

@Service
public class ContractService {
    private final ContractRepository repository;

    public ContractService(ContractRepository repository) { this.repository = repository; }

    public List<Contract> findAll() { return repository.findAll(); }
    public Contract findById(Long id) { return repository.findById(id).orElseThrow(); }

    @Transactional
    public Contract create(Contract c) { return repository.save(c); }

    @Transactional
    public Contract update(Long id, Contract updated) {
        Contract existing = repository.findById(id).orElseThrow();
        existing.setContractNumber(updated.getContractNumber());
        existing.setQuotation(updated.getQuotation());
        existing.setCustomer(updated.getCustomer());
        existing.setContractDate(updated.getContractDate());
        existing.setDeliveryDate(updated.getDeliveryDate());
        existing.setTotalAmount(updated.getTotalAmount());
        existing.setFilePath(updated.getFilePath());
        existing.setStatus(updated.getStatus());
        existing.setDirectorApprovedBy(updated.getDirectorApprovedBy());
        existing.setDirectorApprovedAt(updated.getDirectorApprovedAt());
        existing.setDirectorApprovalNotes(updated.getDirectorApprovalNotes());
        existing.setCreatedBy(updated.getCreatedBy());
        existing.setApprovedBy(updated.getApprovedBy());
        return existing;
    }

    public void delete(Long id) { repository.deleteById(id); }
}


