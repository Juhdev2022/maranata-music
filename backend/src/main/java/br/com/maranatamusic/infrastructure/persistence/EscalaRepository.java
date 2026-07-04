package br.com.maranatamusic.infrastructure.persistence;

import br.com.maranatamusic.domain.Escala;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EscalaRepository extends JpaRepository<Escala, Long> {

    List<Escala> findByUsuarioIdAndCultoDataHoraBetween(
            Long usuarioId, LocalDateTime inicio, LocalDateTime fim);

    boolean existsByCultoIdAndInstrumentoId(Long cultoId, Long instrumentoId);

    // JOIN FETCH obrigatório: sem ele, usuario e instrumento são LAZY e disparam 2 queries por escala (N+1)
    @Query("SELECT e FROM Escala e " +
           "LEFT JOIN FETCH e.usuario " +
           "LEFT JOIN FETCH e.instrumento " +
           "WHERE e.culto.id = :cultoId " +
           "ORDER BY e.id")
    List<Escala> findByCultoIdComUsuarioEInstrumento(@Param("cultoId") Long cultoId);

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END " +
           "FROM Escala e " +
           "WHERE e.usuario.id = :usuarioId " +
           "AND e.culto.dataHora BETWEEN :inicio AND :fim " +
           "AND e.status <> br.com.maranatamusic.domain.enums.EscalaStatus.RECUSADA")
    boolean existeEscalaAtivaNaJanela(
            @Param("usuarioId") Long usuarioId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim);
}
