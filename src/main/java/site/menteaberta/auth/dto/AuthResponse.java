package site.menteaberta.auth.dto;

import site.menteaberta.auth.model.RoleUsuario;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        RoleUsuario role,
        long expiresIn
) {
    public AuthResponse(String accessToken, String refreshToken, RoleUsuario role, long expiresIn) {
        this(accessToken, refreshToken, "Bearer", role, expiresIn);
    }
}
