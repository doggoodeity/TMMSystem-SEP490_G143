package tmmsystem.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tmmsystem.entity.Notification;
import tmmsystem.entity.Rfq;
import tmmsystem.entity.User;
import tmmsystem.entity.Quotation;
import tmmsystem.entity.Contract;
import tmmsystem.repository.NotificationRepository;
import tmmsystem.repository.UserRepository;

import java.time.Instant;
import java.util.List;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Notification createNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    @Transactional
    public void notifyNewRfq(Rfq rfq) {
        // Tìm tất cả Sale Staff
        List<User> saleStaff = userRepository.findByRoleName("SALE_STAFF");
        
        for (User user : saleStaff) {
            Notification notification = new Notification();
            notification.setUser(user);
            notification.setType("INFO");
            notification.setCategory("ORDER");
            notification.setTitle("RFQ mới từ khách hàng");
            notification.setMessage("Có RFQ mới từ khách hàng " + 
                (rfq.getCustomer() != null ? rfq.getCustomer().getCompanyName() : "N/A") + 
                " - RFQ #" + rfq.getRfqNumber());
            notification.setReferenceType("RFQ");
            notification.setReferenceId(rfq.getId());
            notification.setRead(false);
            notification.setCreatedAt(Instant.now());
            
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void notifyRfqForwardedToPlanning(Rfq rfq) {
        // Tìm tất cả Planning Staff
        List<User> planningStaff = userRepository.findByRoleName("PLANNING_STAFF");
        
        for (User user : planningStaff) {
            Notification notification = new Notification();
            notification.setUser(user);
            notification.setType("INFO");
            notification.setCategory("ORDER");
            notification.setTitle("RFQ chuyển đến Planning");
            notification.setMessage("RFQ #" + rfq.getRfqNumber() + " đã được chuyển đến phòng kế hoạch để kiểm tra khả năng sản xuất");
            notification.setReferenceType("RFQ");
            notification.setReferenceId(rfq.getId());
            notification.setRead(false);
            notification.setCreatedAt(Instant.now());
            
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void notifyRfqReceivedByPlanning(Rfq rfq) {
        // Thông báo cho Sale Staff rằng Planning đã nhận RFQ
        List<User> saleStaff = userRepository.findByRoleName("SALE_STAFF");
        
        for (User user : saleStaff) {
            Notification notification = new Notification();
            notification.setUser(user);
            notification.setType("INFO");
            notification.setCategory("ORDER");
            notification.setTitle("RFQ đã được Planning nhận");
            notification.setMessage("RFQ #" + rfq.getRfqNumber() + " đã được phòng kế hoạch nhận và đang xử lý");
            notification.setReferenceType("RFQ");
            notification.setReferenceId(rfq.getId());
            notification.setRead(false);
            notification.setCreatedAt(Instant.now());
            
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void notifyRfqCanceled(Rfq rfq) {
        // Thông báo cho tất cả người liên quan
        List<User> allUsers = userRepository.findAll();
        
        for (User user : allUsers) {
            Notification notification = new Notification();
            notification.setUser(user);
            notification.setType("WARNING");
            notification.setCategory("ORDER");
            notification.setTitle("RFQ đã bị hủy");
            notification.setMessage("RFQ #" + rfq.getRfqNumber() + " đã bị hủy");
            notification.setReferenceType("RFQ");
            notification.setReferenceId(rfq.getId());
            notification.setRead(false);
            notification.setCreatedAt(Instant.now());
            
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void notifyQuotationCreated(Quotation quotation) {
        // Thông báo cho Sale Staff
        List<User> saleStaff = userRepository.findByRoleName("SALE_STAFF");
        
        for (User user : saleStaff) {
            Notification notification = new Notification();
            notification.setUser(user);
            notification.setType("INFO");
            notification.setCategory("ORDER");
            notification.setTitle("Báo giá đã được tạo");
            notification.setMessage("Báo giá #" + quotation.getQuotationNumber() + " đã được Planning tạo từ RFQ #" + 
                (quotation.getRfq() != null ? quotation.getRfq().getRfqNumber() : "N/A"));
            notification.setReferenceType("QUOTATION");
            notification.setReferenceId(quotation.getId());
            notification.setRead(false);
            notification.setCreatedAt(Instant.now());
            
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void notifyQuotationSentToCustomer(Quotation quotation) {
        // Thông báo cho Customer
        if (quotation.getCustomer() != null) {
            Notification notification = new Notification();
            notification.setUser(new User()); // Customer notification
            notification.getUser().setId(quotation.getCustomer().getId());
            notification.setType("INFO");
            notification.setCategory("ORDER");
            notification.setTitle("Báo giá mới");
            notification.setMessage("Bạn có báo giá mới #" + quotation.getQuotationNumber() + " với tổng giá trị " + 
                quotation.getTotalAmount() + " VND");
            notification.setReferenceType("QUOTATION");
            notification.setReferenceId(quotation.getId());
            notification.setRead(false);
            notification.setCreatedAt(Instant.now());
            
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void notifyQuotationApproved(Quotation quotation) {
        // Thông báo cho Sale Staff
        List<User> saleStaff = userRepository.findByRoleName("SALE_STAFF");
        
        for (User user : saleStaff) {
            Notification notification = new Notification();
            notification.setUser(user);
            notification.setType("SUCCESS");
            notification.setCategory("ORDER");
            notification.setTitle("Báo giá được duyệt");
            notification.setMessage("Báo giá #" + quotation.getQuotationNumber() + " đã được khách hàng duyệt");
            notification.setReferenceType("QUOTATION");
            notification.setReferenceId(quotation.getId());
            notification.setRead(false);
            notification.setCreatedAt(Instant.now());
            
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void notifyQuotationRejected(Quotation quotation) {
        // Thông báo cho Sale Staff
        List<User> saleStaff = userRepository.findByRoleName("SALE_STAFF");
        
        for (User user : saleStaff) {
            Notification notification = new Notification();
            notification.setUser(user);
            notification.setType("WARNING");
            notification.setCategory("ORDER");
            notification.setTitle("Báo giá bị từ chối");
            notification.setMessage("Báo giá #" + quotation.getQuotationNumber() + " đã bị khách hàng từ chối");
            notification.setReferenceType("QUOTATION");
            notification.setReferenceId(quotation.getId());
            notification.setRead(false);
            notification.setCreatedAt(Instant.now());
            
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void notifyOrderCreated(Contract contract) {
        // Thông báo cho Sale Staff
        List<User> saleStaff = userRepository.findByRoleName("SALE_STAFF");
        
        for (User user : saleStaff) {
            Notification notification = new Notification();
            notification.setUser(user);
            notification.setType("SUCCESS");
            notification.setCategory("ORDER");
            notification.setTitle("Đơn hàng mới được tạo");
            notification.setMessage("Đơn hàng #" + contract.getContractNumber() + " đã được tạo từ báo giá #" + 
                (contract.getQuotation() != null ? contract.getQuotation().getQuotationNumber() : "N/A"));
            notification.setReferenceType("CONTRACT");
            notification.setReferenceId(contract.getId());
            notification.setRead(false);
            notification.setCreatedAt(Instant.now());
            
            notificationRepository.save(notification);
        }
    }
}
