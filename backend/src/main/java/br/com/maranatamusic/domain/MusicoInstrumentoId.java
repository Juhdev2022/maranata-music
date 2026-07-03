package br.com.maranatamusic.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class MusicoInstrumentoId implements Serializable {

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "instrumento_id")
    private Long instrumentoId;

    public MusicoInstrumentoId() {}

    public MusicoInstrumentoId(Long usuarioId, Long instrumentoId) {
        this.usuarioId = usuarioId;
        this.instrumentoId = instrumentoId;
    }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public Long getInstrumentoId() { return instrumentoId; }
    public void setInstrumentoId(Long instrumentoId) { this.instrumentoId = instrumentoId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MusicoInstrumentoId other)) return false;
        return Objects.equals(usuarioId, other.usuarioId)
            && Objects.equals(instrumentoId, other.instrumentoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usuarioId, instrumentoId);
    }
}
