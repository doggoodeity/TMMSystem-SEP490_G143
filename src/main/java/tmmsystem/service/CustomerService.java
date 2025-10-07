package tmmsystem.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tmmsystem.entity.Customer;
import tmmsystem.entity.User;
import tmmsystem.repository.CustomerRepository;
import tmmsystem.repository.UserRepository;

import java.util.List;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    public CustomerService(CustomerRepository customerRepository, UserRepository userRepository) {
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
    }

    public List<Customer> findAll() { return customerRepository.findAll(); }
    public Customer findById(Long id) { return customerRepository.findById(id).orElseThrow(); }

    @Transactional
    public Customer create(Customer customer, Long createdByUserId) {
        if (createdByUserId != null) {
            User createdBy = userRepository.findById(createdByUserId).orElseThrow();
            customer.setCreatedBy(createdBy);
        }
        return customerRepository.save(customer);
    }

    @Transactional
    public Customer update(Long id, Customer updated) {
        Customer existing = customerRepository.findById(id).orElseThrow();
        existing.setCompanyName(updated.getCompanyName());
        existing.setContactPerson(updated.getContactPerson());
        existing.setEmail(updated.getEmail());
        existing.setPhoneNumber(updated.getPhoneNumber());
        existing.setAddress(updated.getAddress());
        existing.setTaxCode(updated.getTaxCode());
        existing.setActive(updated.getActive());
        existing.setVerified(updated.getVerified());
        existing.setRegistrationType(updated.getRegistrationType());
        return existing;
    }

    public void delete(Long id) { customerRepository.deleteById(id); }
}


