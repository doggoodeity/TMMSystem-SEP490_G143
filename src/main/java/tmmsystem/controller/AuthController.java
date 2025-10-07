// controller/AuthController.java
package tmmsystem.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tmmsystem.dto.auth.LoginRequest;
import tmmsystem.dto.auth.LoginResponse;
import tmmsystem.dto.auth.ChangePasswordRequest;
import tmmsystem.dto.auth.ForgotPasswordRequest;
import tmmsystem.dto.auth.VerifyResetCodeRequest;
import tmmsystem.service.UserService;
import tmmsystem.service.CustomerUserService;
import tmmsystem.dto.auth.CustomerUserRegisterRequest;
import tmmsystem.entity.CustomerUser;

@RestController
@RequestMapping("/v1/auth")
public class AuthController {
    private final UserService userService;
    private final CustomerUserService customerUserService;
    public AuthController(UserService s, CustomerUserService cus){ this.userService = s; this.customerUserService = cus; }

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
    public ResponseEntity<?> registerCustomerUser(@RequestBody CustomerUserRegisterRequest req){
        try {
            CustomerUser cu = new CustomerUser();
            cu.setEmail(req.email());
            cu.setPassword(req.password());
            cu.setName(req.name());
            cu.setPhoneNumber(req.phoneNumber());
            cu.setPosition(req.position());
            customerUserService.create(cu, req.customerId());
            return ResponseEntity.ok("Vui lòng kiểm tra email để xác minh tài khoản");
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
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
