package br.com.maranatamusic.domain;

import br.com.maranatamusic.domain.enums.CultoTipo;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "culto")
public class Culto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CultoTipo tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ministro_id")
    private Usuario ministro;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "repertorio_trancado", nullable = false)
    private boolean repertorioTrancado = false;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }

    public CultoTipo getTipo() { return tipo; }
    public void setTipo(CultoTipo tipo) { this.tipo = tipo; }

    public Usuario getMinistro() { return ministro; }
    public void setMinistro(Usuario ministro) { this.ministro = ministro; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public boolean isRepertorioTrancado() { return repertorioTrancado; }
    public void setRepertorioTrancado(boolean repertorioTrancado) { this.repertorioTrancado = repertorioTrancado; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Culto other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
