package br.com.maranatamusic.infrastructure.persistence;

import br.com.maranatamusic.domain.MusicoInstrumento;
import br.com.maranatamusic.domain.MusicoInstrumentoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MusicoInstrumentoRepository extends JpaRepository<MusicoInstrumento, MusicoInstrumentoId> {

    @Query("SELECT COUNT(m) > 0 FROM MusicoInstrumento m WHERE m.id.instrumentoId = :instrumentoId")
    boolean existsByInstrumentoId(@Param("instrumentoId") Long instrumentoId);

    // JOIN FETCH obrigatório: usuario é LAZY e é lido na listagem de substitutos elegíveis
    @Query("SELECT m FROM MusicoInstrumento m JOIN FETCH m.usuario WHERE m.id.instrumentoId = :instrumentoId")
    List<MusicoInstrumento> findByInstrumentoIdComUsuario(@Param("instrumentoId") Long instrumentoId);

    @Query("SELECT m FROM MusicoInstrumento m WHERE m.id.usuarioId = :usuarioId AND m.principal = true")
    Optional<MusicoInstrumento> findPrincipalPorUsuario(@Param("usuarioId") Long usuarioId);

    // JOIN FETCH obrigatório: usuario e instrumento são LAZY e são lidos no MusicoInstrumentoResponse
    @Query("SELECT mi FROM MusicoInstrumento mi " +
           "JOIN FETCH mi.usuario " +
           "JOIN FETCH mi.instrumento " +
           "WHERE mi.id.usuarioId = :usuarioId " +
           "ORDER BY mi.principal DESC, mi.instrumento.nome ASC")
    List<MusicoInstrumento> findByUsuarioIdComDetalhes(@Param("usuarioId") Long usuarioId);
}
