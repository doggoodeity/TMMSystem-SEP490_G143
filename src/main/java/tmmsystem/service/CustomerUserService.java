package tmmsystem.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tmmsystem.entity.Customer;
import tmmsystem.entity.CustomerUser;
import tmmsystem.repository.CustomerRepository;
import tmmsystem.repository.CustomerUserRepository;
import tmmsystem.service.MailService;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@Service
public class CustomerUserService {
    private final CustomerUserRepository customerUserRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final tmmsystem.util.JwtService jwtService;
    private final MailService mailService;
    private final String appBaseUrl;

    public CustomerUserService(CustomerUserRepository customerUserRepository, CustomerRepository customerRepository, PasswordEncoder passwordEncoder, tmmsystem.util.JwtService jwtService, MailService mailService, @Value("${app.base-url}") String appBaseUrl) {
        this.customerUserRepository = customerUserRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.mailService = mailService;
        this.appBaseUrl = appBaseUrl;
    }

    public List<CustomerUser> findAll() { return customerUserRepository.findAll(); }
    public CustomerUser findById(Long id) { return customerUserRepository.findById(id).orElseThrow(); }
    public boolean existsByEmail(String email) { return customerUserRepository.existsByEmail(email); }

    @Transactional
    public CustomerUser create(CustomerUser user, Long customerId) {
        // Kiểm tra email đã tồn tại
        if (customerUserRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng");
        }
        
        // Nếu có customerId, kiểm tra customer tồn tại
        if (customerId != null) {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new RuntimeException("Customer không tồn tại với ID: " + customerId));
            user.setCustomer(customer);
        }
        // Nếu không có customerId, user.setCustomer(null) - đã mặc định
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // Generate email verification token
        String token = java.util.UUID.randomUUID().toString();
        user.setEmailVerificationToken(token);
        
        CustomerUser saved = customerUserRepository.save(user);
        
        // Send verification email với error handling
        try {
            String link = buildVerificationLink(token);
            String body = "Xin chào " + saved.getName() + ",\n\n" +
                    "Vui lòng xác minh email bằng cách nhấp vào liên kết: " + link + "\n\n" +
                    "Nếu bạn không yêu cầu, vui lòng bỏ qua email này.";
            mailService.send(saved.getEmail(), "Xác minh email", body);
        } catch (Exception ex) {
            // Log error nhưng không rollback transaction
            // User vẫn được tạo, có thể verify sau
            System.err.println("Failed to send verification email: " + ex.getMessage());
        }
        
        return saved;
    }

    @Transactional
    public CustomerUser update(Long id, CustomerUser updated, Long customerId) {
        CustomerUser existing = customerUserRepository.findById(id).orElseThrow();
        if (customerId != null) {
            Customer customer = customerRepository.findById(customerId).orElseThrow();
            existing.setCustomer(customer);
        }
        existing.setEmail(updated.getEmail());
        existing.setName(updated.getName());
        existing.setPhoneNumber(updated.getPhoneNumber());
        existing.setPosition(updated.getPosition());
        existing.setActive(updated.getActive());
        existing.setVerified(updated.getVerified());
        existing.setPrimary(updated.getPrimary());
        if (updated.getPassword() != null && !updated.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(updated.getPassword()));
        }
        return existing;
    }

    public void delete(Long id) { customerUserRepository.deleteById(id); }

    public tmmsystem.dto.auth.LoginResponse authenticate(String email, String password) {
        return customerUserRepository.findByEmail(email)
                .filter(u -> Boolean.TRUE.equals(u.getActive()) && passwordEncoder.matches(password, u.getPassword()))
                .map(u -> {
                    String token = jwtService.generateToken(u.getEmail(), java.util.Map.of(
                            "cuid", u.getId(),
                            "role", "CUSTOMER_USER",
                            "customerId", u.getCustomer() != null ? u.getCustomer().getId() : null
                    ));
                    long expiresIn = jwtService.getExpirationMillis();
                    return new tmmsystem.dto.auth.LoginResponse(u.getId(), u.getName(), u.getEmail(), "CUSTOMER_USER", Boolean.TRUE.equals(u.getActive()), token, expiresIn);
                })
                .orElse(null);
    }

    private String buildVerificationLink(String token) {
        return appBaseUrl + "/v1/auth/customer/verify?token=" + token;
    }

    @Transactional
    public void verifyEmail(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new RuntimeException("Token không hợp lệ");
        }
        
        CustomerUser cu = customerUserRepository.findByEmailVerificationToken(token.trim())
                .orElseThrow(() -> new RuntimeException("Token không hợp lệ hoặc đã hết hạn"));
        
        if (Boolean.TRUE.equals(cu.getVerified())) {
            throw new RuntimeException("Email đã được xác minh trước đó");
        }
        
        cu.setVerified(true);
        cu.setEmailVerificationToken(null);
        customerUserRepository.save(cu);
    }

    @Transactional
    public CustomerUser linkToCustomer(Long customerUserId, Long customerId) {
        CustomerUser user = customerUserRepository.findById(customerUserId)
                .orElseThrow(() -> new RuntimeException("CustomerUser không tồn tại"));
        
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer không tồn tại"));
        
        user.setCustomer(customer);
        return customerUserRepository.save(user);
    }

    public Long getUserIdFromToken(String token) {
        try {
            io.jsonwebtoken.Claims claims = jwtService.parseToken(token);
            String email = claims.getSubject();
            return customerUserRepository.findByEmail(email)
                    .map(CustomerUser::getId)
                    .orElseThrow(() -> new RuntimeException("Token không hợp lệ"));
        } catch (Exception ex) {
            throw new RuntimeException("Token không hợp lệ: " + ex.getMessage());
        }
    }
}


