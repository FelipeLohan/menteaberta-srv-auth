package site.menteaberta.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import site.menteaberta.auth.dto.AuthResponse;
import site.menteaberta.auth.dto.LoginRequest;
import site.menteaberta.auth.dto.RegisterRequest;
import site.menteaberta.auth.exception.EmailJaCadastradoException;
import site.menteaberta.auth.model.RoleUsuario;
import site.menteaberta.auth.model.Usuario;
import site.menteaberta.auth.repository.UsuarioRepository;
import site.menteaberta.auth.security.JwtService;
import site.menteaberta.auth.security.UserDetailsServiceImpl;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "expirationMs", 86400000L);
    }

    @Test
    void registro_emailNovo_sucesso() {
        RegisterRequest request = new RegisterRequest("novo@test.com", "senha123", RoleUsuario.PACIENTE);
        when(usuarioRepository.existsByEmail("novo@test.com")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("hash");
        when(usuarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(jwtService.generateAccessToken(any())).thenReturn("access");
        when(jwtService.generateRefreshToken(any())).thenReturn("refresh");

        AuthResponse response = authService.register(request);

        assertThat(response.accessToken()).isEqualTo("access");
        assertThat(response.refreshToken()).isEqualTo("refresh");
        assertThat(response.role()).isEqualTo(RoleUsuario.PACIENTE);
    }

    @Test
    void registro_emailDuplicado_lancaExcecao() {
        RegisterRequest request = new RegisterRequest("dup@test.com", "senha123", RoleUsuario.PSICOLOGO);
        when(usuarioRepository.existsByEmail("dup@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailJaCadastradoException.class);
    }

    @Test
    void login_credenciaisCorretas_retornaAuthResponse() {
        LoginRequest request = new LoginRequest("user@test.com", "senha123");
        Usuario usuario = Usuario.builder()
                .email("user@test.com").senha("hash").role(RoleUsuario.PACIENTE).ativo(true).build();

        when(userDetailsService.loadUserByUsername("user@test.com")).thenReturn(usuario);
        when(jwtService.generateAccessToken(usuario)).thenReturn("access");
        when(jwtService.generateRefreshToken(usuario)).thenReturn("refresh");

        AuthResponse response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo("access");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_senhaErrada_lancaBadCredentials() {
        LoginRequest request = new LoginRequest("user@test.com", "errada");
        doThrow(new BadCredentialsException("bad")).when(authenticationManager).authenticate(any());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }
}
