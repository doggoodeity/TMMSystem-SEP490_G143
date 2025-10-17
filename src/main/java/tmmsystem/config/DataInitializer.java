package tmmsystem.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import tmmsystem.entity.Role;
import tmmsystem.entity.User;
import tmmsystem.entity.Customer;
import tmmsystem.entity.Product;
import tmmsystem.entity.ProductCategory;
import tmmsystem.repository.RoleRepository;
import tmmsystem.repository.UserRepository;
import tmmsystem.repository.CustomerRepository;
import tmmsystem.repository.MachineRepository;
import tmmsystem.repository.ProductRepository;
import tmmsystem.repository.ProductCategoryRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Random;
import java.math.BigDecimal;


@Component
public class DataInitializer implements CommandLineRunner {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private UserRepository userRepository;

	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	private MachineRepository machineRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductCategoryRepository productCategoryRepository;
    
	@Override
	public void run(String... args) throws Exception {
		createSampleRoles();
		createSampleUsers();
		createSampleCustomer();
		createSampleMachines();
        createSampleCategoriesAndProducts();
	}
    
	private void createSampleRoles() {
		getOrCreateRole("Admin", "Administrator role with full access to all system features");
		getOrCreateRole("Director", "Director role");
		getOrCreateRole("Sale staff", "Sales staff role with access to customer and sales features");
		getOrCreateRole("Planning department", "Planning department role");
		getOrCreateRole("Production manager", "Production manager role");
		getOrCreateRole("Quality Assurance department", "Quality Assurance department role");
		getOrCreateRole("Product Process Leader", "Product Process Leader role");
		getOrCreateRole("Technical Department", "Technical Department role");
		System.out.println("Sample roles ensured successfully!");
	}
    
	private void createSampleUsers() {
		// Admin user with specified email
		createUserIfNotExists(
			"hatsunemikudangyeu06102004@gmail.com",
			"Admin",
			"Nguyễn Văn Admin",
			passwordEncoder.encode("admin123")
		);

		// Sales staff sample user
		createUserIfNotExists(
			"sale@tmms.vn",
			"Sale staff",
			"Trần Thị Sales",
			passwordEncoder.encode("sales123")
		);

		// Director sample user
		createUserIfNotExists(
			"director@tmms.vn",
			"Director",
			"Lê Văn Director",
			passwordEncoder.encode("director123")
		);

		// Planning Department sample user
		createUserIfNotExists(
			"planning@tmms.vn",
			"Planning department",
			"Phạm Thị Planning",
			passwordEncoder.encode("planning123")
		);

		System.out.println("Sample users ensured successfully!");
	}

	private void createSampleCustomer() {
		final String email = "hungnahe180711@fpt.edu.vn";
		if (!customerRepository.existsByEmail(email)) {
			Customer c = new Customer();
			c.setEmail(email);
			c.setCompanyName("Công ty TNHH ABC");
			c.setTaxCode("0123456789");
			c.setBusinessLicense("BL-2024-001");
			c.setAddress("123 Đường ABC, Quận 1, TP.HCM");
			c.setContactPerson("Nguyễn Văn Hùng");
			c.setPhoneNumber("0901234567");
			c.setPosition("Giám đốc");
			c.setPassword(passwordEncoder.encode("customer123"));
			c.setVerified(true);
			c.setActive(true);
			customerRepository.save(c);
		}
	}

	private Role getOrCreateRole(String name, String description) {
		Optional<Role> existing = roleRepository.findByName(name);
		if (existing.isPresent()) {
			return existing.get();
		}
		Role role = new Role();
		role.setName(name);
		role.setDescription(description);
		return roleRepository.save(role);
	}

	private void createUserIfNotExists(String email, String roleName, String name, String encodedPassword) {
		if (userRepository.existsByEmail(email)) return;
		Role role = getOrCreateRole(roleName, roleName + " role");
		User u = new User();
		u.setEmail(email);
		u.setPassword(encodedPassword);
		u.setName(name);
		u.setPhoneNumber("090" + String.format("%07d", RNG.nextInt(10000000)));
		u.setActive(true);
		u.setVerified(true);
		u.setRole(role);
		userRepository.save(u);
	}

	private static final Random RNG = new Random();

	private void createSampleMachines() {
		// 10 weaving machines
		for (int i = 1; i <= 10; i++) {
			createMachineIfNotExists(
				String.format("WEAV-%03d", i),
				"Máy dệt " + i,
				"WEAVING",
				"Khu A-" + (i % 5 + 1)
			);
		}
		// 2 warping machines (máy mắc)
		for (int i = 1; i <= 2; i++) {
			createMachineIfNotExists(
				String.format("WARP-%03d", i),
				"Máy mắc " + i,
				"WARPING",
				"Khu B-" + (i % 2 + 1)
			);
		}
		// 5 sewing machines (máy may)
		for (int i = 1; i <= 5; i++) {
			createMachineIfNotExists(
				String.format("SEW-%03d", i),
				"Máy may " + i,
				"SEWING",
				"Khu C-" + (i % 3 + 1)
			);
		}
	}

	private void createMachineIfNotExists(String code, String name, String type, String location) {
		if (machineRepository.existsByCode(code)) return;
		tmmsystem.entity.Machine m = new tmmsystem.entity.Machine();
		m.setCode(code);
		m.setName(name);
		m.setType(type);
		m.setStatus("AVAILABLE");
		m.setLocation(location);
		m.setSpecifications("{\"brand\":\"TMMS\",\"power\":\"5kW\",\"modelYear\":\"2022\"}");
		Instant now = Instant.now();
		m.setLastMaintenanceAt(now.minus(30 + RNG.nextInt(30), ChronoUnit.DAYS));
		m.setNextMaintenanceAt(now.plus(60 + RNG.nextInt(60), ChronoUnit.DAYS));
		m.setMaintenanceIntervalDays(90);
		machineRepository.save(m);
	}

    private void createSampleCategoriesAndProducts() {
        ProductCategory catMat = getOrCreateCategory("khăn mặt", "Khăn dùng cho mặt");
        ProductCategory catTam = getOrCreateCategory("khăn tắm", "Khăn tắm các kích thước");
        ProductCategory catTheThao = getOrCreateCategory("khăn thể thao", "Khăn dùng cho thể thao");

        // Khăn mặt (category: khăn mặt, code prefix MAT)
        addProductIfNotExists("MAT-001", "Khăn mặt màu cotton cuộn tròn", catMat, "30 x 50 cm", 60, bd(9500));
        addProductIfNotExists("MAT-002", "Khăn mặt hoa bông mềm", catMat, "30 x 50 cm", 60, bd(13000));
        addProductIfNotExists("MAT-003", "Khăn mặt hoa cotton", catMat, "30 x 50 cm", 60, bd(12000));
        addProductIfNotExists("MAT-004", "Khăn mặt hoa cotton + bambo", catMat, "30 x 50 cm", 60, bd(12000));
        addProductIfNotExists("MAT-005", "Khăn mặt hoa bambo", catMat, "30 x 50 cm", 60, bd(12000));
        addProductIfNotExists("MAT-006", "Khăn mặt màu bambo", catMat, "30 x 50 cm", 60, bd(12000));
        addProductIfNotExists("MAT-007", "Khăn mặt màu bông mềm", catMat, "30 x 50 cm", 60, bd(13000));

        // Khăn thể thao (category: khăn thể thao, code prefix SPT)
        addProductIfNotExists("SPT-001", "Khăn thể thao màu cotton", catTheThao, "36 x 80 cm", 100, bd(17000));
        addProductIfNotExists("SPT-002", "Khăn thể thao hoa bông mềm", catTheThao, "35 x 78 cm", 100, bd(20000));
        addProductIfNotExists("SPT-003", "Khăn thể thao hoa bambo", catTheThao, "36 x 80 cm", 100, bd(18000));
        addProductIfNotExists("SPT-004", "Khăn thể thao hoa cotton + bambo", catTheThao, "35 x 78 cm", 100, bd(18000));

        // Khăn tắm (category: khăn tắm, code prefix TAM)
        addProductIfNotExists("TAM-001", "Khăn tắm màu trơn cotton", catTam, "50 x 100 cm", 220, bd(35000));
        addProductIfNotExists("TAM-002", "Khăn tắm màu trơn bambo", catTam, "50 x 100 cm", 220, bd(35000));
        addProductIfNotExists("TAM-003", "Khăn tắm hoa bông mềm", catTam, "50 x 100 cm", 220, bd(42000));
        addProductIfNotExists("TAM-004", "Khăn tắm màu trơn cottton", catTam, "60 x 120 cm", 320, bd(50000));
        addProductIfNotExists("TAM-005", "Khăn tắm màu trơn bambo", catTam, "60 x 120 cm", 320, bd(50000));
        addProductIfNotExists("TAM-006", "Khăn tắm màu trơn cotton", catTam, "70 x 140 cm", 420, bd(65000));
        addProductIfNotExists("TAM-007", "Khăn tắm màu trơn bambo", catTam, "70 x 140 cm", 420, bd(68000));
        addProductIfNotExists("TAM-008", "Khăn tắm hoa bông mềm", catTam, "70 x 140 cm", 420, bd(80000));
        addProductIfNotExists("TAM-009", "Khăn tắm màu bông mềm", catTam, "70 x 140 cm", 420, bd(75000));
        addProductIfNotExists("TAM-010", "Khăn tắm hoa cotton", catTam, "70 x 140 cm", 420, bd(72000));
        addProductIfNotExists("TAM-011", "Khăn tắm hoa cotton + bambo", catTam, "70 x 140 cm", 420, bd(72000));
    }

    private ProductCategory getOrCreateCategory(String name, String description) {
        Optional<ProductCategory> existing = productCategoryRepository.findAll()
            .stream().filter(c -> name.equalsIgnoreCase(c.getName())).findFirst();
        if (existing.isPresent()) return existing.get();
        ProductCategory c = new ProductCategory();
        c.setName(name);
        c.setDescription(description);
        c.setActive(true);
        return productCategoryRepository.save(c);
    }

    private void addProductIfNotExists(String code, String name, ProductCategory category, String dimensions, int weightGrams, BigDecimal price) {
        if (productRepository.existsByCode(code)) return;
        Product p = new Product();
        p.setCode(code);
        p.setName(name);
        p.setCategory(category);
        p.setStandardDimensions(dimensions);
        p.setStandardWeight(new BigDecimal(weightGrams));
        p.setBasePrice(price);
        p.setUnit("CÁI");
        p.setActive(true);
        productRepository.save(p);
    }

    private BigDecimal bd(long vnd) {
        return new BigDecimal(Long.toString(vnd));
    }
}
