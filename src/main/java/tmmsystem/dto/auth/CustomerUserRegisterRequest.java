package tmmsystem.dto.auth;

import jakarta.validation.constraints.*;

public record CustomerUserRegisterRequest(
        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ")
        @Size(max = 150, message = "Email không được quá 150 ký tự")
        String email,
        
        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 6, max = 100, message = "Mật khẩu phải từ 6-100 ký tự")
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{6,}$", 
                 message = "Mật khẩu phải chứa ít nhất 1 chữ cái và 1 số")
        String password,
        
        @NotBlank(message = "Tên không được để trống")
        @Size(max = 150, message = "Tên không được quá 150 ký tự")
        String name,
        
        @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại phải có 10-11 chữ số")
        String phoneNumber,
        
        @Size(max = 100, message = "Chức vụ không được quá 100 ký tự")
        String position,
        
        @NotNull(message = "Customer ID không được để trống")
        @Positive(message = "Customer ID phải là số dương")
        Long customerId
) {}


