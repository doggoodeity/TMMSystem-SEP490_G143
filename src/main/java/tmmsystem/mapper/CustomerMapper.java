package tmmsystem.mapper;

import org.springframework.stereotype.Component;
import tmmsystem.dto.CustomerDto;
import tmmsystem.entity.Customer;

@Component
public class CustomerMapper {
    public CustomerDto toDto(Customer c) {
        if (c == null) return null;
        CustomerDto dto = new CustomerDto();
        dto.setId(c.getId());
        dto.setCompanyName(c.getCompanyName());
        dto.setContactPerson(c.getContactPerson());
        dto.setEmail(c.getEmail());
        dto.setPhoneNumber(c.getPhoneNumber());
        dto.setAddress(c.getAddress());
        dto.setTaxCode(c.getTaxCode());
        dto.setIsActive(c.getActive());
        dto.setIsVerified(c.getVerified());
        dto.setRegistrationType(c.getRegistrationType());
        dto.setCreatedById(c.getCreatedBy() != null ? c.getCreatedBy().getId() : null);
        dto.setCreatedAt(c.getCreatedAt());
        dto.setUpdatedAt(c.getUpdatedAt());
        return dto;
    }

    public Customer toEntity(CustomerDto dto) {
        if (dto == null) return null;
        Customer c = new Customer();
        c.setId(dto.getId());
        c.setCompanyName(dto.getCompanyName());
        c.setContactPerson(dto.getContactPerson());
        c.setEmail(dto.getEmail());
        c.setPhoneNumber(dto.getPhoneNumber());
        c.setAddress(dto.getAddress());
        c.setTaxCode(dto.getTaxCode());
        c.setActive(dto.getIsActive());
        c.setVerified(dto.getIsVerified());
        c.setRegistrationType(dto.getRegistrationType());
        return c;
    }
}


