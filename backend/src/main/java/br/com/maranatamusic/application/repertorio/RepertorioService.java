package br.com.maranatamusic.application.repertorio;

import br.com.maranatamusic.domain.Culto;
import br.com.maranatamusic.domain.Musica;
import br.com.maranatamusic.domain.MusicaCulto;
import br.com.maranatamusic.domain.Usuario;
import br.com.maranatamusic.domain.enums.Papel;
import br.com.maranatamusic.domain.enums.RevisaoLider;
import br.com.maranatamusic.domain.exception.AcessoNaoAutorizadoException;
import br.com.maranatamusic.domain.exception.CultoNaoEncontradoException;
import br.com.maranatamusic.domain.exception.CultoSemMinistroException;
import br.com.maranatamusic.domain.exception.MusicaCultoNaoEncontradaException;
import br.com.maranatamusic.domain.exception.ObservacaoLiderObrigatoriaException;
import br.com.maranatamusic.infrastructure.persistence.CultoRepository;
import br.com.maranatamusic.infrastructure.persistence.MusicaCultoRepository;
import br.com.maranatamusic.infrastructure.persistence.MusicaRepository;
import br.com.maranatamusic.presentation.repertorio.dto.AtualizarMusicaCultoRequest;
import br.com.maranatamusic.presentation.repertorio.dto.CriarMusicaCultoRequest;
import br.com.maranatamusic.presentation.repertorio.dto.MusicaCultoResponse;
import br.com.maranatamusic.presentation.repertorio.dto.RevisaoLiderRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RepertorioService {

    private final CultoRepository cultoRepository;
    private final MusicaRepository musicaRepository;
    private final MusicaCultoRepository musicaCultoRepository;

    public RepertorioService(CultoRepository cultoRepository, MusicaRepository musicaRepository,
                              MusicaCultoRepository musicaCultoRepository) {
        this.cultoRepository = cultoRepository;
        this.musicaRepository = musicaRepository;
        this.musicaCultoRepository = musicaCultoRepository;
    }

    @Transactional(readOnly = true)
    public List<MusicaCultoResponse> listar(Long cultoId, Usuario chamador) {
        Culto culto = buscarCulto(cultoId);
        boolean podeVerObservacao = podeVerObservacao(culto, chamador);

        return musicaCultoRepository.findByCultoIdComMusica(cultoId).stream()
                .map(musicaCulto -> MusicaCultoResponse.from(musicaCulto, podeVerObservacao))
                .toList();
    }

    @Transactional
    public MusicaCultoResponse adicionar(Long cultoId, CriarMusicaCultoRequest request, Usuario chamador) {
        Culto culto = buscarCulto(cultoId);
        validarPodeEditar(culto, chamador);

        if (culto.getMinistro() == null) {
            throw new CultoSemMinistroException();
        }

        Musica musica = resolverMusica(request.titulo(), request.linkVideo());

        MusicaCulto musicaCulto = new MusicaCulto();
        musicaCulto.setCulto(culto);
        musicaCulto.setMusica(musica);
        musicaCulto.setOrdem(request.ordem() != null ? request.ordem() : proximaOrdem(cultoId));
        musicaCulto.setLinkVideoOverride(calcularOverride(musica, request.linkVideo()));

        musicaCultoRepository.save(musicaCulto);
        return MusicaCultoResponse.from(musicaCulto, podeVerObservacao(culto, chamador));
    }

    @Transactional
    public MusicaCultoResponse atualizar(Long cultoId, Long musicaCultoId, AtualizarMusicaCultoRequest request, Usuario chamador) {
        Culto culto = buscarCulto(cultoId);
        validarPodeEditar(culto, chamador);
        MusicaCulto musicaCulto = buscarMusicaCulto(culto, musicaCultoId);

        if (request.linkVideo() != null) {
            musicaCulto.setLinkVideoOverride(calcularOverride(musicaCulto.getMusica(), request.linkVideo()));
        }
        if (request.ordem() != null) {
            musicaCulto.setOrdem(request.ordem());
        }

        musicaCultoRepository.save(musicaCulto);
        return MusicaCultoResponse.from(musicaCulto, podeVerObservacao(culto, chamador));
    }

    @Transactional
    public void remover(Long cultoId, Long musicaCultoId, Usuario chamador) {
        Culto culto = buscarCulto(cultoId);
        validarPodeEditar(culto, chamador);
        MusicaCulto musicaCulto = buscarMusicaCulto(culto, musicaCultoId);
        musicaCultoRepository.delete(musicaCulto);
    }

    @Transactional
    public MusicaCultoResponse revisar(Long cultoId, Long musicaCultoId, RevisaoLiderRequest request) {
        Culto culto = buscarCulto(cultoId);
        MusicaCulto musicaCulto = buscarMusicaCulto(culto, musicaCultoId);

        if (request.revisaoLider() == RevisaoLider.COM_OBSERVACAO
                && (request.observacaoLiderPrivada() == null || request.observacaoLiderPrivada().isBlank())) {
            throw new ObservacaoLiderObrigatoriaException();
        }

        musicaCulto.setRevisaoLider(request.revisaoLider());
        musicaCulto.setObservacaoLiderPrivada(
                request.revisaoLider() == RevisaoLider.APROVADA ? null : request.observacaoLiderPrivada());

        musicaCultoRepository.save(musicaCulto);
        // Quem revisa é sempre LIDER (garantido pelo @PreAuthorize no controller) — sempre vê a observação.
        return MusicaCultoResponse.from(musicaCulto, true);
    }

    private Musica resolverMusica(String titulo, String linkVideo) {
        return musicaRepository.findByTituloIgnoreCase(titulo)
                .orElseGet(() -> {
                    Musica nova = new Musica();
                    nova.setTitulo(titulo);
                    nova.setLinkVideo(linkVideo);
                    return musicaRepository.save(nova);
                });
    }

    private String calcularOverride(Musica musica, String linkVideo) {
        if (linkVideo == null || linkVideo.equals(musica.getLinkVideo())) {
            return null;
        }
        return linkVideo;
    }

    private int proximaOrdem(Long cultoId) {
        return (int) musicaCultoRepository.countByCultoId(cultoId) + 1;
    }

    private Culto buscarCulto(Long cultoId) {
        return cultoRepository.findById(cultoId)
                .orElseThrow(() -> new CultoNaoEncontradoException(cultoId));
    }

    private MusicaCulto buscarMusicaCulto(Culto culto, Long musicaCultoId) {
        MusicaCulto musicaCulto = musicaCultoRepository.findById(musicaCultoId)
                .orElseThrow(() -> new MusicaCultoNaoEncontradaException(musicaCultoId));
        if (!musicaCulto.getCulto().getId().equals(culto.getId())) {
            throw new MusicaCultoNaoEncontradaException(musicaCultoId);
        }
        return musicaCulto;
    }

    private void validarPodeEditar(Culto culto, Usuario chamador) {
        if (!ehMinistroOuLider(culto, chamador)) {
            throw new AcessoNaoAutorizadoException("Você não pode editar o repertório deste culto");
        }
    }

    private boolean podeVerObservacao(Culto culto, Usuario chamador) {
        return ehMinistroOuLider(culto, chamador);
    }

    private boolean ehMinistroOuLider(Culto culto, Usuario chamador) {
        boolean ehMinistro = culto.getMinistro() != null && culto.getMinistro().getId().equals(chamador.getId());
        boolean ehLider = chamador.getPapeis().contains(Papel.LIDER);
        return ehMinistro || ehLider;
    }
}
