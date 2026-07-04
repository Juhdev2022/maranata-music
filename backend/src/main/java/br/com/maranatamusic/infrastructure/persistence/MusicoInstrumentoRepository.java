package br.com.maranatamusic.infrastructure.persistence;

import br.com.maranatamusic.domain.MusicoInstrumento;
import br.com.maranatamusic.domain.MusicoInstrumentoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MusicoInstrumentoRepository extends JpaRepository<MusicoInstrumento, MusicoInstrumentoId> {

    @Query("SELECT COUNT(m) > 0 FROM MusicoInstrumento m WHERE m.id.instrumentoId = :instrumentoId")
    boolean existsByInstrumentoId(@Param("instrumentoId") Long instrumentoId);
}
