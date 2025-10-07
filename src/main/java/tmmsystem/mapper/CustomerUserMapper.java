package tmmsystem.mapper;

import org.springframework.stereotype.Component;
import tmmsystem.dto.CustomerUserDto;
import tmmsystem.entity.Customer;
import tmmsystem.entity.CustomerUser;

@Component
public class CustomerUserMapper {
    public CustomerUserDto toDto(CustomerUser u) {
        if (u == null) return null;
        CustomerUserDto dto = new CustomerUserDto();
        dto.setId(u.getId());
        dto.setEmail(u.getEmail());
        dto.setName(u.getName());
        dto.setPhoneNumber(u.getPhoneNumber());
        dto.setPosition(u.getPosition());
        dto.setCustomerId(u.getCustomer() != null ? u.getCustomer().getId() : null);
        dto.setIsActive(u.getActive());
        dto.setIsVerified(u.getVerified());
        dto.setIsPrimary(u.getPrimary());
        dto.setLastLoginAt(u.getLastLoginAt());
        dto.setCreatedAt(u.getCreatedAt());
        dto.setUpdatedAt(u.getUpdatedAt());
        return dto;
    }

    public CustomerUser toEntity(CustomerUserDto dto) {
        if (dto == null) return null;
        CustomerUser u = new CustomerUser();
        u.setId(dto.getId());
        u.setEmail(dto.getEmail());
        u.setName(dto.getName());
        u.setPhoneNumber(dto.getPhoneNumber());
        u.setPosition(dto.getPosition());
        u.setActive(dto.getIsActive());
        u.setVerified(dto.getIsVerified());
        u.setPrimary(dto.getIsPrimary());
        // customer relation wired in service/controller using customerId
        return u;
    }
}


