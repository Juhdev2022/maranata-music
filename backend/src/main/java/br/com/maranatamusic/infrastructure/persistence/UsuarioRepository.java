package br.com.maranatamusic.infrastructure.persistence;

import br.com.maranatamusic.domain.Usuario;
import br.com.maranatamusic.domain.enums.Papel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    // JOIN FETCH obrigatório: papeis é LAZY e a sessão fecha antes do filtro JWT ler getAuthorities()
    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.papeis WHERE u.email = :email")
    Optional<Usuario> findByEmailComPapeis(@Param("email") String email);

    @Query("SELECT COUNT(u) FROM Usuario u JOIN u.papeis p WHERE p = :papel AND u.ativo = true")
    long countByPapelEAtivo(@Param("papel") Papel papel);
}
