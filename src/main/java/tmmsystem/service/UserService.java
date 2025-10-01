// service/UserService.java
package tmmsystem.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tmmsystem.dto.UserDto;
import tmmsystem.dto.auth.LoginResponse;
import tmmsystem.dto.auth.ForgotPasswordRequest;
import tmmsystem.dto.auth.VerifyResetCodeRequest;
import tmmsystem.dto.auth.ChangePasswordRequest;
import tmmsystem.entity.Role;
import tmmsystem.entity.User;
import tmmsystem.entity.PasswordResetToken;
import tmmsystem.mapper.UserMapper;
import tmmsystem.repository.RoleRepository;
import tmmsystem.repository.UserRepository;
import tmmsystem.repository.PasswordResetTokenRepository;
import tmmsystem.util.JwtService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepo;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final MailService mailService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    
    public UserService(UserRepository userRepo, RoleRepository roleRepository, PasswordEncoder passwordEncoder, JwtService jwtService, MailService mailService, PasswordResetTokenRepository passwordResetTokenRepository) {
        this.userRepo = userRepo;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.mailService = mailService;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    public LoginResponse authenticate(String email, String password){
        return userRepo.findByEmail(email)
                .filter(u -> passwordEncoder.matches(password, u.getPassword()) && Boolean.TRUE.equals(u.getActive()))
                .map(u -> {
                    String token = jwtService.generateToken(u.getEmail(), java.util.Map.of(
                            "uid", u.getId(),
                            "role", u.getRole().getName()
                    ));
                    long expiresIn = jwtService.getExpirationMillis();
                    return new LoginResponse(u.getId(), u.getName(), u.getEmail(), u.getRole().getName(), u.getActive(), token, expiresIn);
                })
                .orElse(null);
    }

    public List<User> findAll(){ return userRepo.findAll(); }

    @Transactional
    public void setActive(Long id, boolean active){
        User u = userRepo.findById(id).orElseThrow();
        u.setActive(active);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = userRepo.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new RuntimeException("User is inactive");
        }
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        if (request.newPassword() == null || request.newPassword().isBlank()) {
            throw new RuntimeException("New password must not be empty");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        // save is optional due to transactional dirty checking but keep explicit
        userRepo.save(user);
    }

    // ===== Forgot password flow =====
    @Transactional
    public void requestPasswordReset(ForgotPasswordRequest request) {
        User user = userRepo.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new RuntimeException("User is inactive");
        }
        // Clean previous/expired tokens for this email
        passwordResetTokenRepository.deleteByEmailOrExpiresAtBefore(user.getEmail(), java.time.Instant.now());

        String code = generateNumericCode(6);
        PasswordResetToken token = new PasswordResetToken();
        token.setEmail(user.getEmail());
        token.setCode(code);
        token.setExpiresAt(java.time.Instant.now().plus(java.time.Duration.ofMinutes(10)));
        token.setUsed(false);
        passwordResetTokenRepository.save(token);

        String subject = "Password Reset Code";
        String body = "Your verification code is: " + code + "\nThis code expires in 10 minutes.";
        mailService.send(user.getEmail(), subject, body);
    }

    @Transactional
    public void verifyCodeAndResetPassword(VerifyResetCodeRequest request) {
        User user = userRepo.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        PasswordResetToken token = passwordResetTokenRepository
                .findByEmailAndCodeAndUsedIsFalse(user.getEmail(), request.code())
                .orElseThrow(() -> new RuntimeException("Invalid or used code"));

        if (java.time.Instant.now().isAfter(token.getExpiresAt())) {
            throw new RuntimeException("Reset code expired");
        }

        String newPasswordPlain = generateRandomPassword(10);
        user.setPassword(passwordEncoder.encode(newPasswordPlain));
        userRepo.save(user);
        token.setUsed(true);
        passwordResetTokenRepository.save(token);

        mailService.send(user.getEmail(), "Your new password", "Your new password is: " + newPasswordPlain);
    }

    private String generateNumericCode(int length) {
        java.security.SecureRandom random = new java.security.SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private String generateRandomPassword(int length) {
        final String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789@#$%";
        java.security.SecureRandom random = new java.security.SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
    public List<UserDto> getAllUsers() {
        return userRepo.findAll()
                .stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    public UserDto getUserById(Long id) {
        return userRepo.findById(id)
                .map(UserMapper::toDto)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public UserDto createUser(User user) {
        if (userRepo.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        Role role = roleRepository.findById(user.getRole().getId())
                .orElseThrow(() -> new RuntimeException("Role not found"));
        user.setRole(role);
        return UserMapper.toDto(userRepo.save(user));
    }

    public UserDto updateUser(Long id, User updated) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setName(updated.getName());
        user.setPhoneNumber(updated.getPhoneNumber());
        user.setAvatar(updated.getAvatar());
        user.setActive(updated.getActive());
        user.setVerified(updated.getVerified());

        if (updated.getPassword() != null && !updated.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(updated.getPassword()));
        }

        if (updated.getRole() != null) {
            Role role = roleRepository.findById(updated.getRole().getId())
                    .orElseThrow(() -> new RuntimeException("Role not found"));
            user.setRole(role);
        }

        return UserMapper.toDto(userRepo.save(user));
    }

    public void deleteUser(Long id) {
        userRepo.deleteById(id);
    }
}
