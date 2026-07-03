package br.com.maranatamusic.infrastructure.persistence;

import br.com.maranatamusic.domain.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class UsuarioRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private UsuarioRepository repository;

    private Usuario novoUsuario() {
        Usuario u = new Usuario();
        u.setNome("Ana Lima");
        u.setEmail("ana@example.com");
        u.setSenhaHash("hash123");
        return u;
    }

    @Test
    void findByEmail_quandoExiste_retornaUsuario() {
        em.persistAndFlush(novoUsuario());

        Optional<Usuario> resultado = repository.findByEmail("ana@example.com");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNome()).isEqualTo("Ana Lima");
    }

    @Test
    void findByEmail_quandoNaoExiste_retornaVazio() {
        Optional<Usuario> resultado = repository.findByEmail("naoexiste@example.com");

        assertThat(resultado).isEmpty();
    }

    @Test
    void existsByEmail_quandoExiste_retornaTrue() {
        em.persistAndFlush(novoUsuario());

        assertThat(repository.existsByEmail("ana@example.com")).isTrue();
    }

    @Test
    void existsByEmail_quandoNaoExiste_retornaFalse() {
        assertThat(repository.existsByEmail("naoexiste@example.com")).isFalse();
    }
}
