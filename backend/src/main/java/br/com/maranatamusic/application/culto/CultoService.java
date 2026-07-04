package br.com.maranatamusic.application.culto;

import br.com.maranatamusic.domain.Culto;
import br.com.maranatamusic.domain.Usuario;
import br.com.maranatamusic.domain.exception.UsuarioNaoEncontradoException;
import br.com.maranatamusic.infrastructure.persistence.CultoRepository;
import br.com.maranatamusic.infrastructure.persistence.UsuarioRepository;
import br.com.maranatamusic.presentation.culto.dto.CriarCultoRequest;
import br.com.maranatamusic.presentation.culto.dto.CultoResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CultoService {

    private final CultoRepository cultoRepository;
    private final UsuarioRepository usuarioRepository;

    public CultoService(CultoRepository cultoRepository, UsuarioRepository usuarioRepository) {
        this.cultoRepository = cultoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public CultoResponse criar(CriarCultoRequest request, Long criadoPorId) {
        Culto culto = new Culto();
        culto.setDataHora(request.dataHora());
        culto.setTipo(request.tipo());
        culto.setObservacoes(request.observacoes());
        culto.setRepertorioTrancado(false);

        if (request.ministroId() != null) {
            Usuario ministro = usuarioRepository.findById(request.ministroId())
                    .orElseThrow(() -> new UsuarioNaoEncontradoException(request.ministroId()));
            culto.setMinistro(ministro);
        }

        cultoRepository.save(culto);
        return CultoResponse.from(culto);
    }
}
