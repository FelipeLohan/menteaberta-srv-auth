package site.menteaberta.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.menteaberta.auth.model.Usuario;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);
}
