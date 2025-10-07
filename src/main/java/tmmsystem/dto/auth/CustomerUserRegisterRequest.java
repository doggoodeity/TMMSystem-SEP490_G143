package tmmsystem.dto.auth;

public record CustomerUserRegisterRequest(
        String email,
        String password,
        String name,
        String phoneNumber,
        String position,
        Long customerId
) {}


