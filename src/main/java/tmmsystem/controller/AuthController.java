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

@RestController
@RequestMapping("/v1/auth")
public class AuthController {
    private final UserService userService;
    public AuthController(UserService s){ this.userService = s; }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req){
        LoginResponse res = userService.authenticate(req.email(), req.password());
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
}
