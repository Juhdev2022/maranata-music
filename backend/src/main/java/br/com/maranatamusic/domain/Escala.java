package br.com.maranatamusic.domain;

import br.com.maranatamusic.domain.enums.EscalaStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "escala")
public class Escala {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "culto_id", nullable = false)
    private Culto culto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrumento_id", nullable = false)
    private Instrumento instrumento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EscalaStatus status = EscalaStatus.PENDENTE;

    @Column(name = "confirmada_em")
    private LocalDateTime confirmadaEm;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Culto getCulto() { return culto; }
    public void setCulto(Culto culto) { this.culto = culto; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Instrumento getInstrumento() { return instrumento; }
    public void setInstrumento(Instrumento instrumento) { this.instrumento = instrumento; }

    public EscalaStatus getStatus() { return status; }
    public void setStatus(EscalaStatus status) { this.status = status; }

    public LocalDateTime getConfirmadaEm() { return confirmadaEm; }
    public void setConfirmadaEm(LocalDateTime confirmadaEm) { this.confirmadaEm = confirmadaEm; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Escala other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
