package tmmsystem.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tmmsystem.entity.*;
import tmmsystem.repository.RfqDetailRepository;
import tmmsystem.repository.RfqRepository;

import java.util.List;

@Service
public class RfqService {
    private final RfqRepository rfqRepository;
    private final RfqDetailRepository detailRepository;

    public RfqService(RfqRepository rfqRepository, RfqDetailRepository detailRepository) {
        this.rfqRepository = rfqRepository;
        this.detailRepository = detailRepository;
    }

    public List<Rfq> findAll() { return rfqRepository.findAll(); }
    public Rfq findById(Long id) { return rfqRepository.findById(id).orElseThrow(); }

    @Transactional
    public Rfq create(Rfq rfq) { return rfqRepository.save(rfq); }

    @Transactional
    public Rfq update(Long id, Rfq updated) {
        Rfq existing = rfqRepository.findById(id).orElseThrow();
        existing.setRfqNumber(updated.getRfqNumber());
        existing.setCustomer(updated.getCustomer());
        existing.setExpectedDeliveryDate(updated.getExpectedDeliveryDate());
        existing.setStatus(updated.getStatus());
        existing.setSent(updated.getSent());
        existing.setNotes(updated.getNotes());
        existing.setCreatedBy(updated.getCreatedBy());
        existing.setApprovedBy(updated.getApprovedBy());
        return existing;
    }

    public void delete(Long id) { rfqRepository.deleteById(id); }
}


