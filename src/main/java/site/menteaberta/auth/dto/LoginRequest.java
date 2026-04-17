package site.menteaberta.auth.dto;

public record LoginRequest(
        String email,
        String senha
) {}
