package br.com.maranatamusic.domain;

import br.com.maranatamusic.domain.enums.RevisaoLider;
import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "musica_culto")
public class MusicaCulto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "culto_id", nullable = false)
    private Culto culto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "musica_id", nullable = false)
    private Musica musica;

    @Column(nullable = false)
    private int ordem;

    @Column(name = "link_video_override", length = 500)
    private String linkVideoOverride;

    @Enumerated(EnumType.STRING)
    @Column(name = "revisao_lider", length = 20)
    private RevisaoLider revisaoLider;

    @Column(name = "observacao_lider_privada", columnDefinition = "TEXT")
    private String observacaoLiderPrivada;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Culto getCulto() { return culto; }
    public void setCulto(Culto culto) { this.culto = culto; }

    public Musica getMusica() { return musica; }
    public void setMusica(Musica musica) { this.musica = musica; }

    public int getOrdem() { return ordem; }
    public void setOrdem(int ordem) { this.ordem = ordem; }

    public String getLinkVideoOverride() { return linkVideoOverride; }
    public void setLinkVideoOverride(String linkVideoOverride) { this.linkVideoOverride = linkVideoOverride; }

    public RevisaoLider getRevisaoLider() { return revisaoLider; }
    public void setRevisaoLider(RevisaoLider revisaoLider) { this.revisaoLider = revisaoLider; }

    public String getObservacaoLiderPrivada() { return observacaoLiderPrivada; }
    public void setObservacaoLiderPrivada(String observacaoLiderPrivada) { this.observacaoLiderPrivada = observacaoLiderPrivada; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MusicaCulto other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
