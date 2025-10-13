package tmmsystem.dto.auth;

public record CustomerLoginResponse(
    Long userId,
    String name,
    String email,
    String role,
    boolean active,
    String accessToken,
    long expiresIn,
    Long customerId,
    String companyName
) {}
