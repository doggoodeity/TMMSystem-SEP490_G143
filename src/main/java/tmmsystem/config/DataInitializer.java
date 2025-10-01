package tmmsystem.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import tmmsystem.entity.Role;
import tmmsystem.entity.User;
import tmmsystem.repository.RoleRepository;
import tmmsystem.repository.UserRepository;


@Component
public class DataInitializer implements CommandLineRunner {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public void run(String... args) throws Exception {
        // Tạo các role mẫu nếu chưa tồn tại
        if (roleRepository.count() == 0) {
            createSampleRoles();
        }
        
        // Tạo các user mẫu nếu chưa tồn tại
        if (userRepository.count() == 0) {
            createSampleUsers();
        }
    }
    
    private void createSampleRoles() {
        // Tạo role ADMIN
        Role adminRole = new Role();
        adminRole.setName("ADMIN");
        adminRole.setDescription("Administrator role with full access to all system features");
        roleRepository.save(adminRole);
        
        // Tạo role SALES
        Role salesRole = new Role();
        salesRole.setName("SALES");
        salesRole.setDescription("Sales role with access to customer management and sales features");
        roleRepository.save(salesRole);
        
        // Tạo role MANAGER
        Role managerRole = new Role();
        managerRole.setName("MANAGER");
        managerRole.setDescription("Manager role with access to team management and reporting");
        roleRepository.save(managerRole);
        
        // Tạo role USER
        Role userRole = new Role();
        userRole.setName("USER");
        userRole.setDescription("Regular user role with basic access");
        roleRepository.save(userRole);
        
        System.out.println("Sample roles created successfully!");
    }
    
    private void createSampleUsers() {
        // Lấy các role đã tạo
        Role adminRole = roleRepository.findByName("ADMIN").orElse(null);
        Role salesRole = roleRepository.findByName("SALES").orElse(null);
        Role managerRole = roleRepository.findByName("MANAGER").orElse(null);
        Role userRole = roleRepository.findByName("USER").orElse(null);
        
        // Tạo Admin User
        User adminUser = new User();
        adminUser.setName("System Administrator");
        adminUser.setEmail("hatsunemikudangyeu06102004@gmail.com");
        adminUser.setPassword(passwordEncoder.encode("admin123"));


        adminUser.setPhoneNumber("+84 123 456 789");
        adminUser.setAvatar("https://via.placeholder.com/150/0000FF/FFFFFF?text=Admin");
        adminUser.setActive(true);
        adminUser.setVerified(true);
        adminUser.setRole(adminRole);
        userRepository.save(adminUser);
        
        // Tạo Sales User
        User salesUser = new User();
        salesUser.setName("Sales Manager");
        salesUser.setEmail("sales@tmm.com");
        salesUser.setPassword(passwordEncoder.encode("sales123"));

        salesUser.setPhoneNumber("+84 987 654 321");
        salesUser.setAvatar("https://via.placeholder.com/150/00FF00/FFFFFF?text=Sales");
        salesUser.setActive(true);
        salesUser.setVerified(true);
        salesUser.setRole(salesRole);
        userRepository.save(salesUser);
        
        // Tạo Manager User
        User managerUser = new User();
        managerUser.setName("Project Manager");
        managerUser.setEmail("manager@tmm.com");
        managerUser.setPassword(passwordEncoder.encode("manager123"));

        managerUser.setPhoneNumber("+84 555 123 456");
        managerUser.setAvatar("https://via.placeholder.com/150/FFA500/FFFFFF?text=Manager");
        managerUser.setActive(true);
        managerUser.setVerified(true);
        managerUser.setRole(managerRole);
        userRepository.save(managerUser);
        
        // Tạo Regular User
        User regularUser = new User();
        regularUser.setName("John Doe");
        regularUser.setEmail("john.doe@tmm.com");
        regularUser.setPassword(passwordEncoder.encode("user123"));

        regularUser.setPhoneNumber("+84 111 222 333");
        regularUser.setAvatar("https://via.placeholder.com/150/FF0000/FFFFFF?text=User");
        regularUser.setActive(true);
        regularUser.setVerified(false);
        regularUser.setRole(userRole);
        userRepository.save(regularUser);
        
        // Tạo thêm một User khác
        User anotherUser = new User();
        anotherUser.setName("Jane Smith");
        anotherUser.setEmail("jane.smith@tmm.com");
        anotherUser.setPassword(passwordEncoder.encode("user456"));
        anotherUser.setPhoneNumber("+84 444 555 666");
        anotherUser.setAvatar("https://via.placeholder.com/150/800080/FFFFFF?text=Jane");
        anotherUser.setActive(false);
        anotherUser.setVerified(false);
        anotherUser.setRole(userRole);
        userRepository.save(anotherUser);
        
        System.out.println("Sample users created successfully!");
    }
}
