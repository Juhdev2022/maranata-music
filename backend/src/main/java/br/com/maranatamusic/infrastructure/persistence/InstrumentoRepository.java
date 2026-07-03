package br.com.maranatamusic.infrastructure.persistence;

import br.com.maranatamusic.domain.Instrumento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstrumentoRepository extends JpaRepository<Instrumento, Long> {
}
