package site.menteaberta.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import site.menteaberta.auth.dto.AuthResponse;
import site.menteaberta.auth.dto.LoginRequest;
import site.menteaberta.auth.dto.RegisterRequest;
import site.menteaberta.auth.exception.EmailJaCadastradoException;
import site.menteaberta.auth.model.Usuario;
import site.menteaberta.auth.repository.UsuarioRepository;
import site.menteaberta.auth.security.JwtService;
import site.menteaberta.auth.security.UserDetailsServiceImpl;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    public AuthResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new EmailJaCadastradoException(request.email());
        }

        Usuario usuario = Usuario.builder()
                .email(request.email())
                .senha(passwordEncoder.encode(request.senha()))
                .role(request.role())
                .ativo(true)
                .build();

        usuarioRepository.save(usuario);

        String accessToken = jwtService.generateAccessToken(usuario);
        String refreshToken = jwtService.generateRefreshToken(usuario);
        return new AuthResponse(accessToken, refreshToken, usuario.getRole(), expirationMs);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha())
        );

        Usuario usuario = (Usuario) userDetailsService.loadUserByUsername(request.email());
        String accessToken = jwtService.generateAccessToken(usuario);
        String refreshToken = jwtService.generateRefreshToken(usuario);
        return new AuthResponse(accessToken, refreshToken, usuario.getRole(), expirationMs);
    }

    public AuthResponse refresh(String refreshToken) {
        String email = jwtService.extractUsername(refreshToken);
        Usuario usuario = (Usuario) userDetailsService.loadUserByUsername(email);

        if (!jwtService.isTokenValid(refreshToken, usuario)) {
            throw new IllegalArgumentException("Refresh token inválido ou expirado");
        }

        String newAccessToken = jwtService.generateAccessToken(usuario);
        String newRefreshToken = jwtService.generateRefreshToken(usuario);
        return new AuthResponse(newAccessToken, newRefreshToken, usuario.getRole(), expirationMs);
    }
}
