package br.com.maranatamusic.domain;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "musico_instrumento")
public class MusicoInstrumento {

    @EmbeddedId
    private MusicoInstrumentoId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("usuarioId")
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("instrumentoId")
    @JoinColumn(name = "instrumento_id")
    private Instrumento instrumento;

    @Column(nullable = false)
    private boolean principal = false;

    public MusicoInstrumentoId getId() { return id; }
    public void setId(MusicoInstrumentoId id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Instrumento getInstrumento() { return instrumento; }
    public void setInstrumento(Instrumento instrumento) { this.instrumento = instrumento; }

    public boolean isPrincipal() { return principal; }
    public void setPrincipal(boolean principal) { this.principal = principal; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MusicoInstrumento other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
