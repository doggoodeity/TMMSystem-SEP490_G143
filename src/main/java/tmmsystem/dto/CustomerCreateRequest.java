package tmmsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CustomerCreateRequest(
        @NotBlank(message = "Tên công ty không được để trống")
        @Size(max = 255, message = "Tên công ty không được quá 255 ký tự")
        String companyName,
        
        @Size(max = 150, message = "Người liên hệ không được quá 150 ký tự")
        String contactPerson,
        
        @Size(max = 150, message = "Email không được quá 150 ký tự")
        String email,
        
        @Size(max = 30, message = "Số điện thoại không được quá 30 ký tự")
        String phoneNumber,
        
        String address,
        
        @Size(max = 50, message = "Mã số thuế không được quá 50 ký tự")
        String taxCode,
        
        Boolean isActive,
        Boolean isVerified,
        
        @Size(max = 20, message = "Loại đăng ký không được quá 20 ký tự")
        String registrationType,
        
        @NotNull(message = "Created by ID không được để trống")
        Long createdById
) {}
