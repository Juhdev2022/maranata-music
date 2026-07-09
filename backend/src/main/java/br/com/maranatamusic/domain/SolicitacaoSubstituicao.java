package br.com.maranatamusic.domain;

import br.com.maranatamusic.domain.enums.MotivoSubstituicao;
import br.com.maranatamusic.domain.enums.SolicitacaoStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "solicitacao_substituicao")
public class SolicitacaoSubstituicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escala_id", nullable = false)
    private Escala escala;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitante_id", nullable = false)
    private Usuario solicitante;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MotivoSubstituicao motivo;

    @Column(columnDefinition = "TEXT")
    private String observacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "substituto_sugerido_id")
    private Usuario substitutoSugerido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SolicitacaoStatus status = SolicitacaoStatus.ABERTA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "substituto_final_id")
    private Usuario substitutoFinal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aprovada_por_id")
    private Usuario aprovadaPor;

    @Column(name = "resolvida_em")
    private LocalDateTime resolvidaEm;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Escala getEscala() { return escala; }
    public void setEscala(Escala escala) { this.escala = escala; }

    public Usuario getSolicitante() { return solicitante; }
    public void setSolicitante(Usuario solicitante) { this.solicitante = solicitante; }

    public MotivoSubstituicao getMotivo() { return motivo; }
    public void setMotivo(MotivoSubstituicao motivo) { this.motivo = motivo; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }

    public Usuario getSubstitutoSugerido() { return substitutoSugerido; }
    public void setSubstitutoSugerido(Usuario substitutoSugerido) { this.substitutoSugerido = substitutoSugerido; }

    public SolicitacaoStatus getStatus() { return status; }
    public void setStatus(SolicitacaoStatus status) { this.status = status; }

    public Usuario getSubstitutoFinal() { return substitutoFinal; }
    public void setSubstitutoFinal(Usuario substitutoFinal) { this.substitutoFinal = substitutoFinal; }

    public Usuario getAprovadaPor() { return aprovadaPor; }
    public void setAprovadaPor(Usuario aprovadaPor) { this.aprovadaPor = aprovadaPor; }

    public LocalDateTime getResolvidaEm() { return resolvidaEm; }
    public void setResolvidaEm(LocalDateTime resolvidaEm) { this.resolvidaEm = resolvidaEm; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SolicitacaoSubstituicao other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
