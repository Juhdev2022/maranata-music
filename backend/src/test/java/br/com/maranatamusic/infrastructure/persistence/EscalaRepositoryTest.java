package br.com.maranatamusic.infrastructure.persistence;

import br.com.maranatamusic.domain.*;
import br.com.maranatamusic.domain.enums.CategoriaInstrumento;
import br.com.maranatamusic.domain.enums.CultoTipo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class EscalaRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private EscalaRepository repository;

    private Usuario usuario;
    private Instrumento instrumento;
    private Culto culto;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setNome("Carlos");
        usuario.setEmail("carlos@example.com");
        usuario.setSenhaHash("hash");
        em.persist(usuario);

        instrumento = new Instrumento();
        instrumento.setNome("Violão");
        instrumento.setCategoria(CategoriaInstrumento.CORDA);
        em.persist(instrumento);

        culto = new Culto();
        culto.setDataHora(LocalDateTime.of(2026, 7, 6, 9, 0));
        culto.setTipo(CultoTipo.DOMINGO_MANHA);
        em.persist(culto);

        em.flush();
    }

    private Escala novaEscala() {
        Escala e = new Escala();
        e.setCulto(culto);
        e.setUsuario(usuario);
        e.setInstrumento(instrumento);
        return e;
    }

    @Test
    void findByUsuarioIdAndCultoDataHoraBetween_quandoDentroDoIntervalo_retornaEscala() {
        em.persistAndFlush(novaEscala());

        List<Escala> resultado = repository.findByUsuarioIdAndCultoDataHoraBetween(
                usuario.getId(),
                LocalDateTime.of(2026, 7, 1, 0, 0),
                LocalDateTime.of(2026, 7, 31, 23, 59)
        );

        assertThat(resultado).hasSize(1);
    }

    @Test
    void findByUsuarioIdAndCultoDataHoraBetween_quandoForaDoIntervalo_retornaVazio() {
        em.persistAndFlush(novaEscala());

        List<Escala> resultado = repository.findByUsuarioIdAndCultoDataHoraBetween(
                usuario.getId(),
                LocalDateTime.of(2026, 8, 1, 0, 0),
                LocalDateTime.of(2026, 8, 31, 23, 59)
        );

        assertThat(resultado).isEmpty();
    }

    @Test
    void existsByCultoIdAndInstrumentoId_quandoExiste_retornaTrue() {
        em.persistAndFlush(novaEscala());

        assertThat(repository.existsByCultoIdAndInstrumentoId(
                culto.getId(), instrumento.getId())).isTrue();
    }

    @Test
    void existsByCultoIdAndInstrumentoId_quandoNaoExiste_retornaFalse() {
        assertThat(repository.existsByCultoIdAndInstrumentoId(
                culto.getId(), instrumento.getId())).isFalse();
    }
}
