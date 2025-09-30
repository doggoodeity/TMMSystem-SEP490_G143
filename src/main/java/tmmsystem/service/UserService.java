// service/UserService.java
package tmmsystem.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tmmsystem.dto.UserDto;
import tmmsystem.dto.auth.LoginResponse;
import tmmsystem.dto.auth.ChangePasswordRequest;
import tmmsystem.entity.Role;
import tmmsystem.entity.User;
import tmmsystem.mapper.UserMapper;
import tmmsystem.repository.RoleRepository;
import tmmsystem.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepo;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    
    public UserService(UserRepository userRepo, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse authenticate(String email, String password){
        return userRepo.findByEmail(email)
                .filter(u -> passwordEncoder.matches(password, u.getPassword()) && Boolean.TRUE.equals(u.getActive()))
                .map(u -> new LoginResponse(u.getId(), u.getName(), u.getEmail(), u.getRole().getName(), u.getActive()))
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
