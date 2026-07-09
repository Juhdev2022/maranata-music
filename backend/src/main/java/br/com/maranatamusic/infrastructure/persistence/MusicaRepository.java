package br.com.maranatamusic.infrastructure.persistence;

import br.com.maranatamusic.domain.Musica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MusicaRepository extends JpaRepository<Musica, Long> {

    Optional<Musica> findByTituloIgnoreCase(String titulo);
}
