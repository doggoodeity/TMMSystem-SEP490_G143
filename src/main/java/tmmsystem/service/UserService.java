// service/UserService.java
package tmmsystem.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tmmsystem.dto.auth.LoginResponse;
import tmmsystem.entity.User;
import tmmsystem.repository.UserRepository;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepo;
    public UserService(UserRepository userRepo){ this.userRepo = userRepo; }

    public LoginResponse authenticate(String email, String password){
        return userRepo.findByEmail(email)
                .filter(u -> u.getPassword().equals(password) && Boolean.TRUE.equals(u.getActive()))
                .map(u -> new LoginResponse(u.getId(), u.getName(), u.getEmail(), u.getRole().getName(), u.getActive()))
                .orElse(null);
    }

    public List<User> findAll(){ return userRepo.findAll(); }

    @Transactional
    public void setActive(Long id, boolean active){
        User u = userRepo.findById(id).orElseThrow();
        u.setActive(active);
    }
}
