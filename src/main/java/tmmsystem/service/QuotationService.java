package tmmsystem.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tmmsystem.entity.Quotation;
import tmmsystem.repository.QuotationRepository;

import java.util.List;

@Service
public class QuotationService {
    private final QuotationRepository repository;

    public QuotationService(QuotationRepository repository) { this.repository = repository; }

    public List<Quotation> findAll() { return repository.findAll(); }
    public Quotation findById(Long id) { return repository.findById(id).orElseThrow(); }

    @Transactional
    public Quotation create(Quotation q) { return repository.save(q); }

    @Transactional
    public Quotation update(Long id, Quotation updated) {
        Quotation existing = repository.findById(id).orElseThrow();
        existing.setQuotationNumber(updated.getQuotationNumber());
        existing.setRfq(updated.getRfq());
        existing.setCustomer(updated.getCustomer());
        existing.setValidUntil(updated.getValidUntil());
        existing.setTotalAmount(updated.getTotalAmount());
        existing.setStatus(updated.getStatus());
        existing.setAccepted(updated.getAccepted());
        existing.setCanceled(updated.getCanceled());
        existing.setCapacityCheckedBy(updated.getCapacityCheckedBy());
        existing.setCapacityCheckedAt(updated.getCapacityCheckedAt());
        existing.setCapacityCheckNotes(updated.getCapacityCheckNotes());
        existing.setCreatedBy(updated.getCreatedBy());
        existing.setApprovedBy(updated.getApprovedBy());
        return existing;
    }

    public void delete(Long id) { repository.deleteById(id); }
}


