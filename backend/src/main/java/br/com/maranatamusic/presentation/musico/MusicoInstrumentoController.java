package br.com.maranatamusic.presentation.musico;

import br.com.maranatamusic.application.musico.MusicoInstrumentoService;
import br.com.maranatamusic.application.musico.ResultadoVinculo;
import br.com.maranatamusic.presentation.musico.dto.MusicoInstrumentoResponse;
import br.com.maranatamusic.presentation.musico.dto.VincularMusicoInstrumentoRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/musicos")
public class MusicoInstrumentoController {

    private final MusicoInstrumentoService musicoInstrumentoService;

    public MusicoInstrumentoController(MusicoInstrumentoService musicoInstrumentoService) {
        this.musicoInstrumentoService = musicoInstrumentoService;
    }

    @PostMapping("/{usuarioId}/instrumentos/{instrumentoId}")
    @PreAuthorize("hasRole('LIDER')")
    public ResponseEntity<MusicoInstrumentoResponse> vincular(
            @PathVariable Long usuarioId,
            @PathVariable Long instrumentoId,
            @RequestBody(required = false) VincularMusicoInstrumentoRequest request) {
        boolean principal = request != null && request.principal();
        ResultadoVinculo resultado = musicoInstrumentoService.vincular(usuarioId, instrumentoId, principal);
        HttpStatus status = resultado.criado() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(resultado.resposta());
    }

    @DeleteMapping("/{usuarioId}/instrumentos/{instrumentoId}")
    @PreAuthorize("hasRole('LIDER')")
    public ResponseEntity<Void> desvincular(@PathVariable Long usuarioId, @PathVariable Long instrumentoId) {
        musicoInstrumentoService.desvincular(usuarioId, instrumentoId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{usuarioId}/instrumentos")
    public ResponseEntity<List<MusicoInstrumentoResponse>> listarPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(musicoInstrumentoService.listarPorUsuario(usuarioId));
    }
}
