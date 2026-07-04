package br.com.maranatamusic.infrastructure.persistence;

import br.com.maranatamusic.domain.MusicoInstrumento;
import br.com.maranatamusic.domain.MusicoInstrumentoId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MusicoInstrumentoRepository extends JpaRepository<MusicoInstrumento, MusicoInstrumentoId> {
}
