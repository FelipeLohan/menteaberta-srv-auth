package site.menteaberta.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import site.menteaberta.auth.model.RoleUsuario;

public record RegisterRequest(
        @NotBlank @Email String email,
        @NotBlank String senha,
        @NotNull RoleUsuario role
) {}
