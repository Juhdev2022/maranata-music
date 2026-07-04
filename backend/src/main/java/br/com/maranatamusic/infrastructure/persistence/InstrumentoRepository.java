package br.com.maranatamusic.infrastructure.persistence;

import br.com.maranatamusic.domain.Instrumento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InstrumentoRepository extends JpaRepository<Instrumento, Long> {

    boolean existsByNomeIgnoreCase(String nome);

    List<Instrumento> findAllByOrderByCategoriaAscNomeAsc();
}
