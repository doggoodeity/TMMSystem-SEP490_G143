package tmmsystem.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tmmsystem.entity.Contract;
import tmmsystem.entity.User;
import tmmsystem.repository.ContractRepository;
import tmmsystem.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ContractService {
    private final ContractRepository repository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final MinIOFileService minIOFileService;

    public ContractService(ContractRepository repository, 
                         UserRepository userRepository,
                         NotificationService notificationService,
                         MinIOFileService minIOFileService) { 
        this.repository = repository; 
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.minIOFileService = minIOFileService;
    }

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

    // ===== GIAI ĐOẠN 3: CONTRACT UPLOAD & APPROVAL =====
    
    @Transactional
    public Contract uploadSignedContract(Long contractId, MultipartFile file, String notes, Long saleUserId) {
        Contract contract = repository.findById(contractId)
            .orElseThrow(() -> new RuntimeException("Contract not found"));
        
        try {
            // Upload file to MinIO
            String filePath = minIOFileService.uploadContractFile(file, contractId);
            
            // Update contract
            contract.setFilePath(filePath);
            contract.setStatus("PENDING_APPROVAL");
            contract.setUpdatedAt(Instant.now());
            
            Contract savedContract = repository.save(contract);
            
            // Send notification to Director
            notificationService.notifyContractUploaded(savedContract);
            
            return savedContract;
        } catch (Exception e) {
            log.error("Error uploading contract file", e);
            throw new RuntimeException("Failed to upload contract file: " + e.getMessage(), e);
        }
    }
    
    @Transactional
    public Contract approveContract(Long contractId, Long directorId, String notes) {
        Contract contract = repository.findById(contractId)
            .orElseThrow(() -> new RuntimeException("Contract not found"));
        
        User director = userRepository.findById(directorId)
            .orElseThrow(() -> new RuntimeException("Director not found"));
        
        contract.setStatus("APPROVED");
        contract.setDirectorApprovedBy(director);
        contract.setDirectorApprovedAt(Instant.now());
        contract.setDirectorApprovalNotes(notes);
        contract.setUpdatedAt(Instant.now());
        
        Contract savedContract = repository.save(contract);
        
        // Send notification to Planning Department
        notificationService.notifyContractApproved(savedContract);
        
        return savedContract;
    }
    
    @Transactional
    public Contract rejectContract(Long contractId, Long directorId, String rejectionNotes) {
        Contract contract = repository.findById(contractId)
            .orElseThrow(() -> new RuntimeException("Contract not found"));
        
        User director = userRepository.findById(directorId)
            .orElseThrow(() -> new RuntimeException("Director not found"));
        
        contract.setStatus("REJECTED");
        contract.setDirectorApprovedBy(director);
        contract.setDirectorApprovedAt(Instant.now());
        contract.setDirectorApprovalNotes(rejectionNotes);
        contract.setUpdatedAt(Instant.now());
        
        Contract savedContract = repository.save(contract);
        
        // Send notification to Sale Staff
        notificationService.notifyContractRejected(savedContract);
        
        return savedContract;
    }
    
    public List<Contract> getContractsPendingApproval() {
        return repository.findByStatus("PENDING_APPROVAL");
    }
    
    public List<Contract> getDirectorPendingContracts() {
        return repository.findByStatus("PENDING_APPROVAL");
    }
    
    public String getContractFileUrl(Long contractId) {
        try {
            return minIOFileService.getContractFileUrl(contractId);
        } catch (Exception e) {
            log.error("Error getting contract file URL for contract ID: {}", contractId, e);
            return null;
        }
    }
    
    public InputStream downloadContractFile(Long contractId) {
        try {
            return minIOFileService.downloadContractFile(contractId);
        } catch (Exception e) {
            log.error("Error downloading contract file for contract ID: {}", contractId, e);
            throw new RuntimeException("Failed to download contract file: " + e.getMessage(), e);
        }
    }
}


