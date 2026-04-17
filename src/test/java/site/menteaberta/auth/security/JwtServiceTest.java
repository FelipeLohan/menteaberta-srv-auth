package site.menteaberta.auth.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import site.menteaberta.auth.model.RoleUsuario;
import site.menteaberta.auth.model.Usuario;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;

    private static final String SECRET = Base64.getEncoder().encodeToString(
            "uma-chave-secreta-de-pelo-menos-32-bytes-ok!".getBytes()
    );

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 86400000L, 604800000L);
    }

    @Test
    void gerarToken_extrairUsername_validar() {
        Usuario usuario = Usuario.builder()
                .email("test@test.com")
                .senha("hash")
                .role(RoleUsuario.PACIENTE)
                .ativo(true)
                .build();

        String token = jwtService.generateAccessToken(usuario);

        assertThat(jwtService.extractUsername(token)).isEqualTo("test@test.com");
        assertThat(jwtService.isTokenValid(token, usuario)).isTrue();
    }

    @Test
    void tokenExpirado_deveRetornarInvalido() {
        JwtService shortLived = new JwtService(SECRET, -1000L, -1000L);

        Usuario usuario = Usuario.builder()
                .email("expiry@test.com")
                .senha("hash")
                .role(RoleUsuario.PSICOLOGO)
                .ativo(true)
                .build();

        String token = shortLived.generateAccessToken(usuario);

        assertThatThrownBy(() -> jwtService.isTokenValid(token, usuario))
                .isInstanceOf(Exception.class);
    }
}
