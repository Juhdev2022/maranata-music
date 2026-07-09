package br.com.maranatamusic.infrastructure.persistence;

import br.com.maranatamusic.domain.MusicaCulto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MusicaCultoRepository extends JpaRepository<MusicaCulto, Long> {

    long countByCultoId(Long cultoId);

    // JOIN FETCH obrigatório: musica é LAZY e é lida no MusicaCultoResponse
    @Query("SELECT mc FROM MusicaCulto mc JOIN FETCH mc.musica WHERE mc.culto.id = :cultoId ORDER BY mc.ordem ASC")
    List<MusicaCulto> findByCultoIdComMusica(@Param("cultoId") Long cultoId);
}
