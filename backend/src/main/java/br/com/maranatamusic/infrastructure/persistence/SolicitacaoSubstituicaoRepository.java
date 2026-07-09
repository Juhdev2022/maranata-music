package br.com.maranatamusic.infrastructure.persistence;

import br.com.maranatamusic.domain.SolicitacaoSubstituicao;
import br.com.maranatamusic.domain.enums.SolicitacaoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SolicitacaoSubstituicaoRepository extends JpaRepository<SolicitacaoSubstituicao, Long> {

    boolean existsByEscalaIdAndStatus(Long escalaId, SolicitacaoStatus status);

    // JOIN FETCH obrigatório: escala, culto, instrumento, usuario da escala e solicitante são LAZY
    @Query("SELECT s FROM SolicitacaoSubstituicao s " +
           "JOIN FETCH s.escala e " +
           "JOIN FETCH e.culto " +
           "JOIN FETCH e.instrumento " +
           "JOIN FETCH e.usuario " +
           "JOIN FETCH s.solicitante " +
           "LEFT JOIN FETCH s.substitutoSugerido " +
           "WHERE s.status = :status " +
           "ORDER BY s.id")
    List<SolicitacaoSubstituicao> findByStatusComDetalhes(@Param("status") SolicitacaoStatus status);
}
