package br.com.maranatamusic.infrastructure.persistence;

import br.com.maranatamusic.domain.Culto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CultoRepository extends JpaRepository<Culto, Long> {

    List<Culto> findByDataHoraBetween(LocalDateTime inicio, LocalDateTime fim);
}
