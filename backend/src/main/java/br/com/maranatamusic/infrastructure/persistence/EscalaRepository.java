package br.com.maranatamusic.infrastructure.persistence;

import br.com.maranatamusic.domain.Escala;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EscalaRepository extends JpaRepository<Escala, Long> {

    List<Escala> findByUsuarioIdAndCultoDataHoraBetween(
            Long usuarioId, LocalDateTime inicio, LocalDateTime fim);

    boolean existsByCultoIdAndInstrumentoId(Long cultoId, Long instrumentoId);
}
