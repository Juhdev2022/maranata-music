package br.com.maranatamusic.presentation.repertorio;

import br.com.maranatamusic.application.repertorio.RepertorioService;
import br.com.maranatamusic.infrastructure.security.CustomUserDetails;
import br.com.maranatamusic.presentation.repertorio.dto.AtualizarMusicaCultoRequest;
import br.com.maranatamusic.presentation.repertorio.dto.CriarMusicaCultoRequest;
import br.com.maranatamusic.presentation.repertorio.dto.MusicaCultoResponse;
import br.com.maranatamusic.presentation.repertorio.dto.RevisaoLiderRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cultos/{cultoId}/repertorio")
public class RepertorioController {

    private final RepertorioService repertorioService;

    public RepertorioController(RepertorioService repertorioService) {
        this.repertorioService = repertorioService;
    }

    @GetMapping
    public ResponseEntity<List<MusicaCultoResponse>> listar(@PathVariable Long cultoId,
                                                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(repertorioService.listar(cultoId, userDetails.getUsuario()));
    }

    @PostMapping
    public ResponseEntity<MusicaCultoResponse> adicionar(@PathVariable Long cultoId,
                                                          @Valid @RequestBody CriarMusicaCultoRequest request,
                                                          @AuthenticationPrincipal CustomUserDetails userDetails) {
        MusicaCultoResponse response = repertorioService.adicionar(cultoId, request, userDetails.getUsuario());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{musicaCultoId}")
    public ResponseEntity<MusicaCultoResponse> atualizar(@PathVariable Long cultoId,
                                                          @PathVariable Long musicaCultoId,
                                                          @RequestBody AtualizarMusicaCultoRequest request,
                                                          @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(repertorioService.atualizar(cultoId, musicaCultoId, request, userDetails.getUsuario()));
    }

    @DeleteMapping("/{musicaCultoId}")
    public ResponseEntity<Void> remover(@PathVariable Long cultoId,
                                         @PathVariable Long musicaCultoId,
                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        repertorioService.remover(cultoId, musicaCultoId, userDetails.getUsuario());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{musicaCultoId}/revisao-lider")
    @PreAuthorize("hasRole('LIDER')")
    public ResponseEntity<MusicaCultoResponse> revisar(@PathVariable Long cultoId,
                                                        @PathVariable Long musicaCultoId,
                                                        @Valid @RequestBody RevisaoLiderRequest request) {
        return ResponseEntity.ok(repertorioService.revisar(cultoId, musicaCultoId, request));
    }
}
