// controller/AuthController.java
package tmmsystem.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import tmmsystem.dto.auth.LoginRequest;
import tmmsystem.dto.auth.LoginResponse;
import tmmsystem.dto.auth.ChangePasswordRequest;
import tmmsystem.dto.auth.ForgotPasswordRequest;
import tmmsystem.dto.auth.VerifyResetCodeRequest;
import tmmsystem.service.UserService;
import tmmsystem.service.CustomerUserService;
import tmmsystem.dto.auth.CustomerUserRegisterRequest;
import tmmsystem.dto.CustomerCreateRequest;
import tmmsystem.entity.CustomerUser;
import tmmsystem.entity.Customer;
import tmmsystem.service.CustomerService;

@RestController
@RequestMapping("/v1/auth")
@Validated
public class AuthController {
    private final UserService userService;
    private final CustomerUserService customerUserService;
    private final CustomerService customerService;
    public AuthController(UserService s, CustomerUserService cus, CustomerService cs){ 
        this.userService = s; 
        this.customerUserService = cus; 
        this.customerService = cs;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req){
        LoginResponse res = userService.authenticate(req.email(), req.password());
        if (res == null) {
            res = customerUserService.authenticate(req.email(), req.password());
        }
        if (res == null) return ResponseEntity.status(401).body("Invalid credentials or inactive");
        return ResponseEntity.ok(res);
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest req){
        try {
            userService.changePassword(req);
            return ResponseEntity.ok().build();
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest req){
        try {
            userService.requestPasswordReset(req);
            return ResponseEntity.ok().build();
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/verify-reset-code")
    public ResponseEntity<?> verifyResetCode(@RequestBody VerifyResetCodeRequest req){
        try {
            userService.verifyCodeAndResetPassword(req);
            return ResponseEntity.ok().build();
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/customer/register")
    public ResponseEntity<?> registerCustomerUser(@Valid @RequestBody CustomerUserRegisterRequest req){
        try {
            // Kiểm tra email đã tồn tại chưa
            if (customerUserService.existsByEmail(req.email())) {
                return ResponseEntity.badRequest().body("Email đã được sử dụng");
            }
            
            CustomerUser cu = new CustomerUser();
            cu.setEmail(req.email().toLowerCase().trim());
            cu.setPassword(req.password());
            cu.setName(null);
            cu.setPhoneNumber(null);
            cu.setPosition(null);
            
            CustomerUser savedUser = customerUserService.create(cu, null);
            return ResponseEntity.ok("Vui lòng kiểm tra email để xác minh tài khoản");
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body("Lỗi đăng ký: " + ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body("Lỗi hệ thống: " + ex.getMessage());
        }
    }

    @PostMapping("/customer/create-company")
    public ResponseEntity<?> createCompany(@Valid @RequestBody CustomerCreateRequest req, 
                                         @RequestHeader("Authorization") String authHeader) {
        try {
            // Extract user ID from JWT token
            String token = authHeader.replace("Bearer ", "");
            Long userId = customerUserService.getUserIdFromToken(token);
            
            // Kiểm tra user đã có customer chưa
            CustomerUser user = customerUserService.findById(userId);
            if (user.getCustomer() != null) {
                return ResponseEntity.badRequest().body("Bạn đã có công ty rồi");
            }
            
            // Tạo Customer
            Customer customer = new Customer();
            customer.setCompanyName(req.companyName().trim());
            customer.setContactPerson(req.contactPerson() != null ? req.contactPerson().trim() : null);
            customer.setEmail(user.getEmail());
            customer.setPhoneNumber(req.phoneNumber());
            customer.setAddress(req.address());
            customer.setTaxCode(req.taxCode());
            customer.setActive(true);
            customer.setVerified(false);
            customer.setRegistrationType("SELF_REGISTERED");
            
            Customer savedCustomer = customerService.create(customer, null);
            
            // Liên kết CustomerUser với Customer
            customerUserService.linkToCustomer(userId, savedCustomer.getId());
            
            return ResponseEntity.ok("Tạo công ty thành công");
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body("Lỗi tạo công ty: " + ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body("Lỗi hệ thống: " + ex.getMessage());
        }
    }

    @GetMapping("/customer/verify")
    public ResponseEntity<?> verifyCustomerEmail(@RequestParam("token") String token) {
        try {
            customerUserService.verifyEmail(token);
            return ResponseEntity.ok("Email đã được xác minh thành công");
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
